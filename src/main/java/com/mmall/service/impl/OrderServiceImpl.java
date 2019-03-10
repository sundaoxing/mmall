package com.mmall.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayResponse;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.*;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderCartProductVo;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {

    private static  AlipayTradeService tradeService;
    static {

        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayInfoMapper payInfoMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ShippingMapper shippingMapper;
    /*
                        前台模块
     */
    /**
     *业务1：创建订单
     *      1.调用DAO层接口，根据用户Id查询该用户的已勾选的购物车List列表
     *      2.调用getCartToOrderItem(userId,cartList)方法，组装OrderItem的List列表
     *      3.判断OrderItem的List列表是否为空
     *          是：返回购物车为空错误信息
     *      3.计算订单条目OrderItem的List列表中所有商品的总价格
     *      4.调用assembleOrder(userId,shippingId,payment)方法，组装Order对象
     *      5.判断order对象是否为空
     *          是：返回订单创建失败错误信息
     *      6.遍历OrderItem的List列表
     *          将每一个OrderItem对象设置订单号
     *      7.调用DAO层接口，将OrderItem批量插入到数据库中
     *      8.调用reduceProductStock(orderItemList)方法，减少该订单中所有商品的库存量
     *      9.清空购物车
     *      10.调用assembleOrderVo(order,orderItemList)方法，组装OrderVo对象
     *      11.返回OrderVO给前端
     * @param userId        用户Id
     * @param shippingId    收货地址Id
     * @return
     */
    @Override
    public ServerResponse createOrder(Integer userId, Integer shippingId) {
        List<Cart> cartList = cartMapper.selectCheckedByUserId(userId);
        ServerResponse serverResponse = this.getCartToOrderItem(userId,cartList);
        if(!serverResponse.isSuccess()){
            return serverResponse;
        }
        List<OrderItem>orderItemList = (List<OrderItem>) serverResponse.getData();
        if(CollectionUtils.isEmpty(orderItemList)){
            return ServerResponse.createByErrorMsg("购物车为空");
        }
        BigDecimal payment=this.getOrderTotalPrice(orderItemList);

        Order order = assembleOrder(userId,shippingId,payment);
        if(order ==null){
            return ServerResponse.createByErrorMsg("订单生成失败");
        }

        for(OrderItem orderItem :orderItemList){
            orderItem.setOrderNo(order.getOrderNo());
        }
        orderItemMapper.batchInsert(orderItemList);

        this.reduceProductStock(orderItemList);

        this.cleanCart(cartList);

        OrderVo orderVo = assembleOrderVo(order,orderItemList);

        return ServerResponse.createBySuccess(orderVo);
    }

    /**
     * 业务2：取消订单
     *      1.调用DAO层接口，根据用户Id，订单号获取订单Order对象
     *      2.判断订单Order是否为空
     *          是：返回无此订单错误信息
     *      3.判断订单Order的状态值是否大于等于支付状态值
     *          是：返回已付款，无法取消错误消息
     *      4.创建新的订单Order对象，用于更新订单order的状态
     *      5.设置新的订单Order对象的Id属性值
     *      6.设置新的订单Order对象的状态为取消订单状态
     *      7.调用DAO层接口，更新订单的状态
     *      8.判断更新结果的返回值是否大于0
     *          是：返回取消订单成功的信息
     *          都:返回取消订单失败的信息
     * @param userId        用户Id
     * @param orderNo       订单号
     * @return
     */
    @Override
    public ServerResponse cancel(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if(order ==null){
            return ServerResponse.createByErrorMsg("无此订单");
        }
        if(order.getStatus() >=OrderStatus.PAID.getCode()){
            return ServerResponse.createByErrorMsg("已付款，无法取消");
        }
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(OrderStatus.CANCELED.getCode());
        int rowCount =orderMapper.updateByPrimaryKeySelective(updateOrder);
        if(rowCount >0){
            return ServerResponse.createBySuccessMsg("取消订单成功");
        }
        return ServerResponse.createByErrorMsg("取消订单失败");
    }

    /**
     * 业务3：获取订单中商品信息
     *      1.调用DAO层接口，根据用户Id获取购物车中已经勾选的Cart对象的List列表
     *      2.调用getCartToOrderItem(userId,cartList)方法，组装OrderItem的List列表
     *      3.判断组装OrderItem的List列表是否不成功
     *          是：直接返回错误信息
     *      4.创建OrderCartProductVo对象
     *      5.从getCartToOrderItem(userId,cartList)方法的返回值中获取OrderItem对象的List列表
     *      6.创建OrderItemVo对象的List列表
     *      7.初始化付款金额payment为0
     *      8.遍历OrderList对象的List列表
     *          1.计算付款金额：付款金额=初始化付款金额+OrderItem订单条目的总价格（累加）
     *          2.使用OrderItem组装OrderItemVo对象
     *          3.将OrderItemVo对象添加到OrderItemVo对象的List列表中
     *      9.设置OrderCartProductVo对象的属性值
     *      10.将OrderCartProduct对象返回给前端
     * @param userId    用户Id
     * @return
     */
    @Override
    public ServerResponse getOrderCartProduct(Integer userId) {
        List<Cart> cartList = cartMapper.selectCheckedByUserId(userId);
        ServerResponse serverResponse=this.getCartToOrderItem(userId,cartList);
        if(!serverResponse.isSuccess()){
            return serverResponse;
        }
        OrderCartProductVo orderCartProductVo = new OrderCartProductVo();
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();
        List<OrderItemVo> orderItemVoList = new ArrayList<>();
        BigDecimal payment = new BigDecimal("0");
        for(OrderItem orderItem : orderItemList){
            //计算订单总价格：总价格=总价格初始化+订单条目的总价格（累加）
            payment = BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
            OrderItemVo orderItemVo = assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderCartProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        orderCartProductVo.setOrderItemVoList(orderItemVoList);
        orderCartProductVo.setProductTotalPrice(payment);
        return ServerResponse.createBySuccess(orderCartProductVo);
    }

    /**
     * 使用Order，OrderItem的List列表组装OrderVo对象
     *      1.创建OrderVo对象
     *      2.设置OrderVo对象的部分属性值
     *      3.调用DAO层接口，根据ShippingId获取Shipping对象
     *      4.判断shipping是否不为空
     *          是：1.设置OrderVO的收货地址
     *              2.使用shipping对象组装ShippingVo对象，设置OrderVO的ShippingVo收货详情
     *      5.设置OrderVo的时间相关的属性值
     *      6.创建OrderItemVo对象的List列表
     *      7.遍历OrderItem对象的List列表
     *          1.使用OrderItem组装成OrderItemVo
     *          2.添加到OrderItemVo对象的List列表中
     *      8.设置OrderVo的OrderItemVOList属性值
     *      9.返回OrderVo对象
     * @param order             Order对象
     * @param orderItemList     OrderItem对象的列表
     * @return
     */
    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList) {
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(PaymentType.getDescByCode(order.getPaymentType()).getDesc());

        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(OrderStatus.getDescByCode(order.getStatus()).getDesc());

        orderVo.setShippingId(order.getShippingId());
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if(shipping !=null){
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(assembleShippingVo(shipping));
        }

        orderVo.setPaymentTime(DateTimeUtil.dateToStrStandard(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStrStandard(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStrStandard(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStrStandard(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStrStandard(order.getCloseTime()));

        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        List<OrderItemVo> orderItemVoList = new ArrayList<>();
        for(OrderItem orderItem : orderItemList){
            OrderItemVo orderItemVo= assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderVo.setOrderItemVoList(orderItemVoList);
        return orderVo;
    }

    /**
     * 使用OrderItem对象组装OrderItemVo对象
     *      1.创建OrderItemVo对象
     *      2.设置OrderItemVo对象属性值
     *      3.返回OrderItemVo对象
     * @param orderItem     OrderItem对象
     * @return
     */
    private OrderItemVo assembleOrderItemVo(OrderItem orderItem) {
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());

        orderItemVo.setCreateTime(DateTimeUtil.dateToStrStandard(orderItem.getCreateTime()));
        return orderItemVo;
    }

    /**
     * 使用Shipping对象组装ShippingVo对象
     *      1.创建ShippingVo对象
     *      2.设置ShippingVo对象的属性值
     *      3.返回ShippingVo对象
     * @param shipping  收货地址Shipping对象
     * @return
     */
    private ShippingVo assembleShippingVo(Shipping shipping) {
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        return shippingVo;
    }

    /**
     * 清空购物车
     *      1.遍历购物车对象Cart的List列表
     *          调用DAO层接口，根据Cart对象的id删除该条购物车记录
     * @param cartList      Cart对象的List列表
     */
    private void cleanCart(List<Cart> cartList) {
        for(Cart cartItem : cartList){
            cartMapper.deleteByPrimaryKey(cartItem.getId());
        }
    }


    /**
     * 减少该订单中所有商品的库存量
     *      1.遍历OrderItem的List列表
     *          1.调用DAO层接口，根据ProductId获取Product商品对象
     *          2.设置该商品的库存量（当前库存量=以前库存量-用户订单购买量）
     *          3.调用DAO层接口，更新该Product对象的库存量值
     * @param orderItemList     OrderItem对象的List列表
     */
    private void reduceProductStock(List<OrderItem> orderItemList) {
        for(OrderItem orderItem :orderItemList){
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            //当前库存量=以前库存量-用户订单购买量
            product.setStock(product.getStock()-orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    /**
     * 使用用户Id，收货地址Id，支付总金额组装Order对象
     *      1.创建Order对象
     *      2.生成订单号
     *      3.设置Order的属性值
     *      4.调用DAO层接口，插入新生成的订单Order对象到数据库中
     *      5.判断插入结果是否成功
     *          是：返回新生成的订单Order对象
     *          否：返回null
     * @param userId        用户Id
     * @param shippingId    收货地址Id
     * @param payment       支付金额
     * @return
     */
    private Order assembleOrder(Integer userId, Integer shippingId, BigDecimal payment) {
        Order order = new Order();
        Long orderNo = generateOrderNo();
        order.setOrderNo(orderNo);
        order.setStatus(OrderStatus.NO_PAY.getCode());
        order.setPostage(0);
        order.setPaymentType(PaymentType.ONLINE_PAY.getCode());
        order.setPayment(payment);
        order.setUserId(userId);
        order.setShippingId(shippingId);
        int rowCount =orderMapper.insert(order);
        if(rowCount > 0){
            return order;
        }
        return null;
    }

    /**
     * 订单号生成：
     *      1.获取系统当前时间
     *      2.系统当前时间+0-100随机数作为订单号
     * @return
     */
    private Long generateOrderNo() {
        Long currentTime = System.currentTimeMillis();
        return currentTime+new Random().nextInt(100);
    }

    /**
     * 获取订单的总价格：
     *      1.初始化订单总价格
     *      2.遍历OrderItem的List列表
     *          将每一个订单条目的价格累加
     *      3.返回订单总价格
     * @param orderItemList     OrderItem对象的List列表
     * @return
     */
    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList) {
        BigDecimal payment = new BigDecimal("0");
        for(OrderItem orderItem : orderItemList){
            payment =BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        return payment;
    }

    /**
     * 使用userId和Cart的List列表组装OrderItem的List列表
     *      1.创建OrderItem的List列表对象
     *      2.判断购物车列表cartList是否为空
     *          是：返回购物车为空错误信息
     *      3.遍历购物车列表cartList
     *          1.调用DAO层接口，根据商品Id，获取Product对象
     *          2.判断商品的状态值是否不等于商品在售状态值
     *              是：返回商品下架错误信息
     *          3.判断购物车中该商品购买数量是否大于该商品的库存数量
     *              是：返回商品库存不足错误信息
     *          4.创建OrderItem对象
     *          5.设置OrderItem的属性值（组装OrderItem对象）
     *          6.将OrderItem对象添加到OrderItem的List列表中
     *      4.返回OrderItem的List列表
     * @param userId        用户Id
     * @param cartList      Cart对象List列表
     * @return
     */
    private ServerResponse getCartToOrderItem(Integer userId,List<Cart> cartList) {
        List<OrderItem>orderItemList = new ArrayList<>();
        if(CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByErrorMsg("购物车为空");
        }
        for(Cart cartItem :cartList){
            Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            if(ProductStatus.On_SALE.getCode() != product.getStatus()){
                return ServerResponse.createByErrorMsg("商品已下架");
            }
            if(cartItem.getQuantity() > product.getStock()){
                return ServerResponse.createByErrorMsg("商品库存不足");
            }
            OrderItem orderItem = new OrderItem();
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            //设置订单中该商品条目的总价格：商品单价X购买数量
            orderItem.setTotalPrice(BigDecimalUtil.multiply(product.getPrice().doubleValue(),cartItem.getQuantity()));
            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);
    }


    /**
     * 业务4：获取订单详情
     *      1.调用DAO层接口，根据用户Id，订单号获取订单Order对象
     *      2.判断订单Order是否不为空
     *          是：1.调用DAO层接口，根据用户Id，订单号获取OrderItem对象的List列表
     *              2.调用assembleOrderVo(order,orderItemList)方法，组装OrderVo对象
     *              3.返回OrderVo对象给前端
     *      3.返回没有此订单错误信息
     * @param userId        用户Id
     * @param orderNo       订单号
     * @return
     */
    @Override
    public ServerResponse orderDetail(Integer userId,Long orderNo){
        Order order =orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if(order != null){
            List<OrderItem> orderItemList=orderItemMapper.selectByUserIdAndOrderNo(userId,orderNo);
            OrderVo orderVo = assembleOrderVo(order,orderItemList);
            return ServerResponse.createBySuccess(orderVo);
        }
        return ServerResponse.createBySuccessMsg("没有此订单");
    }

    /**
     * 业务5：获取订单列表
     *      1.使用PageHelper设置分页参数
     *      2.调用DAO层接口，根据用户Id，获取订单Order的List列表
     *      3.调用assembleOrderVoList(userId,orderList)方法，组装OrderVo对象的List列表
     *      4.使用PageInfo开始对Order对象的List列表分页
     *      5.使用OrderVo的List列表替换分页结果中的orderList列表
     *      6.将分页结果返回给前端
     * @param userId        用户Id
     * @param pageNum       第几页，默认第1页
     * @param pageSize      每页大小，每页显示多少条订单order信息，默认10条
     * @return
     */
    @Override
    public ServerResponse orderList(Integer userId,Integer pageNum,Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectByUserId(userId);
        List<OrderVo> orderVoList = assembleOrderVoList(userId,orderList);
        PageInfo resultPage = new PageInfo(orderList);
        resultPage.setList(orderVoList);
        return ServerResponse.createBySuccess(resultPage);
    }

    /**
     * 使用用户Id，Order对象的List列表组装OrderVO对象的List列表
     *      1.创建OrderVo对象的List列表
     *      2.遍历Order对象的List列表
     *          1.初始化OrderItem对象的List列表
     *          2.判断userId是否为空
     *              是：调用DAO层接口，根据订单号获取订单条目OrderItem的List列表
     *              否：调用DAO层接口，根据用户Id，订单号获取订单条目OrderItem的List列表
     *          3.调用assembleOrderVo(order,orderItemList)方法，组装OrderVo对象
     *          4.将OrderVo添加到OrderItem的List列表中
     *      3.返回OrderItem对象的List列表
     *      方法复用：
     *当 userId == null 时  ->    说明是管理员用户，返回该订单Order下所有的订单条目OrderItem
     *当 userId != null 时  ->    说明是普通用户，返回该订单Order下所有属于该用户的的订单条目OrderItem
     * @param userId
     * @param orderList
     * @return
     */
    private List<OrderVo> assembleOrderVoList(Integer userId,List<Order> orderList) {
        List<OrderVo> orderVoList = new ArrayList<>();
        for(Order order:orderList){
            List<OrderItem> orderItemList=null;
            if(userId ==null){//管理员
                orderItemList=orderItemMapper.selectByOrderNo(order.getOrderNo());
            }else{//普通用户
                orderItemList = orderItemMapper.selectByUserIdAndOrderNo(userId,order.getOrderNo());
            }
            OrderVo orderVo = assembleOrderVo(order,orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }


    /**
                    支付接口
        用户：
                1.对购物车中勾选商品提交订单
        1.预下单：（服务器端）
                1.调用支付宝接口：（当面付2.0）生成支付二维码
                2.将支付二维码上传到ftp服务器上
                3.前端访问ftp服务器，显示支付二维码
        2.付款：
                1.用户扫码付款，支付宝处理订单信息
                2.支付宝回调，返回付款结果
                3.验证支付宝回调的合法性
                4.订单状态更新，保存支付信息
                5.返回验证结果
     */
    /**
     * 业务1：付款
     *      1.调用DAO层接口，查询订单，根据用户Id和订单号
     *      2.判断订单Order是否为空
     *          是：返回错误信息，没有此订单
     *          否：1.构建响应值->Key：value键值对Map对象
     *              2.将orderNo：oder.getOrderNo()键值对放入Map中
     *              3.调用trade_precreate(order,path,resultMap)方法生成付款二维码
     * @param orderNo   订单号
     * @param userId    用户Id
     * @param path      付款二维码存储路径（本地）
     * @return
     */
    @Override
    public ServerResponse pay(Long orderNo, Integer userId, String path) {
        Order order  = orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if(order == null){
            return ServerResponse.createByErrorMsg("当前用户没有此订单");
        }
        Map<String,String> resultMap = Maps.newHashMap();
        resultMap.put("orderNo",String.valueOf(order.getOrderNo()));
        return this.trade_precreate(order,path,resultMap);
    }

    /**
     * 业务2：支付宝回调处理
     *      1.从request域中获取支付宝成功处理订单后的返回信息(key:value对)Map对象
     *      2.遍历requestMap对象
     *          1.获取key，再根据key获取value数组
     *          2.遍历value数组，将value数组中每个元素重新拼接成一个以","为值分隔符的新字符串
     *          3.将key和value数组拼接后的字符串存入新的属性Map对象中
     *      3.调用logger打印支付宝回调相关参数信息
     *      4.移除key为"sign_type"的value值
     *      5.验签：使用支付宝公钥，对支付宝返回的参数信息进行校验
     *      6.判断校验结果是否合法
     *          1.不合法：直接返回错误信息
     *      7.获取订单号，交易号，交易状态
     *      8.调用DAO层接口，根据订单号查询订单
     *      9.判断订单Order对象是否为空
     *          是：返回支付宝回调错误信息
     *      10.判断订单状态值是否大于等于支付状态值
     *          是：返回支付宝重复回调信息
     *      11.判断交易状态是否为成功交易
     *          是：更新订单的交易时间和交易状态
     *      12.构造支付信息PayInfo对象，设置其相关属性值
     *      13.调用DAO层接口，插入新的payInfo信息
     *      14.返回支付宝回调成功信息
     * @param request   HttpServletRequest对象（包含支付宝成功处理订单后的返回信息）
     * @return
     */
    @Override
    public ServerResponse alipayCallback(HttpServletRequest request) {
        Map requestParamsMap = request.getParameterMap();
        Map<String,String> paramsMap = Maps.newHashMap();
        for(Iterator iterator=requestParamsMap.keySet().iterator();iterator.hasNext();){
            String name = (String) iterator.next();
            String [] values =(String []) requestParamsMap.get(name);
            String valueStr = "";
            for(int i=0 ;i<values.length;i++){
                valueStr= (i==values.length-1)?(valueStr+values[i]):(valueStr+values[i])+",";
            }
            paramsMap.put(name,valueStr);
        }
        logger.info("支付宝回调：sign:{},trade_status:{},参数:{}",paramsMap.get("sign"),paramsMap.get("trade_status"),paramsMap.toString());
        paramsMap.remove("sign_type");
        try {
            //支付宝返回参数，校验
            boolean alipayRSACheckedV2 = AlipaySignature.rsaCheckV2(paramsMap, Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());
            if(!alipayRSACheckedV2){
                return ServerResponse.createByErrorMsg("非法请求，已报警");
            }
        } catch (AlipayApiException e) {
            logger.error("支付宝回调验证异常",e);
        }
        Long orderNo = Long.parseLong(paramsMap.get("out_trade_no"));
        String tradeNo = paramsMap.get("trade_no");
        String tradeStatus = paramsMap.get("trade_status");
        Order order=orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMsg("支付宝非法回调订单,忽略");
        }
        //只要订单状态值大于等于支付状态值，就认为用户已经付过款了，支付宝属于重复回调
        if(order.getStatus() >= OrderStatus.PAID.getCode()){
            return ServerResponse.createBySuccess("支付宝重复回调");
        }
        //用户支付成功，更新订单状态
        if(Const.AlipayCallback.TRADE_SUCCESS.equals(tradeStatus)){
            order.setPaymentTime(DateTimeUtil.strToDateStandard(paramsMap.get("gmt_payment")));
            order.setStatus(OrderStatus.PAID.getCode());
            orderMapper.updateByPrimaryKeySelective(order);
        }

        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(PayPlatform.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);

        payInfoMapper.insert(payInfo);
        return ServerResponse.createBySuccess();
    }

    /**
     * 业务3：查询订单支付状态
     *      1.调用DAO层接口，更具用户Id和订单号查询订单
     *      2.判断订单Order对象是否为空
     *          是：返回错误信息
     *      3.判断订单Order的状态值是否大于等于订单支付状态值
     *          是：返回成功支付信息
     *          否：返回未支付信息
     * @param userId    用户Id
     * @param orderNo   订单号
     * @return
     */
    @Override
    public ServerResponse queryOrderPayStatus(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if(order ==null){
            return ServerResponse.createByErrorMsg("用户无此订单");
        }
        if(order.getStatus() >= OrderStatus.PAID.getCode()){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }


    /**
     *当面付2.0生成支付二维码（支付宝支付DEMO中源码）
     * @param order         订单Order对象
     * @param path          付款二维码存储路径（本地）
     * @param resultMap     响应值Map对象
     * @return
     */
    private ServerResponse trade_precreate(Order order,String path,Map<String,String> resultMap){
        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo =order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店消费”
        String subject = "happyMmall网上商城订单";

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选，根据需要决定是否使用) 订单可打折金额，可以配合商家平台配置折扣活动，如果订单部分商品参与打折，可以将部分商品总价填写至此字段，默认全部商品可打折
        // 如果该值未传入,但传入了【订单总金额】,【不可打折金额】 则该值默认为【订单总金额】- 【不可打折金额】
        //        String discountableAmount = "1.00"; //

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品3件共20.00元"
        String body =new StringBuilder().append("订单").append(outTradeNo).append("购买商品共").append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        String providerId = "2088100200300400500";
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId(providerId);

        // 支付超时，支付扫码交易定义为15分钟
        String timeoutExpress = "15m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        //订单明细表，一个订单的所有订单条目
        List<OrderItem> orderItemList = orderItemMapper.selectByUserIdAndOrderNo(order.getUserId(),order.getOrderNo());

        //遍历OrderItem的List列表，组装GoodsDetail
        for(OrderItem orderItem :orderItemList){
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
            GoodsDetail goods = GoodsDetail.newInstance(orderItem.getProductId().toString(),orderItem.getProductName(),
                    BigDecimalUtil.multiply(orderItem.getCurrentUnitPrice().doubleValue(),new Double(100)).longValue(),orderItem.getQuantity());
            // 创建好一个商品后添加至商品明细列表
            goodsDetailList.add(goods);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);
        //获取预下单，支付宝处理结果
        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");
                //获取支付宝成功处理响应数据
                AlipayTradePrecreateResponse response = result.getResponse();
                //简单打印支付宝成功处理响应数据
                dumpResponse(response);
                //调用makeQr(path,resultMap,response)方法，生成付款二维码
                return this.makeQr(path,resultMap,response);

            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMsg("支付宝预下单失败!!!");

            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMsg("系统异常，预下单状态未知!!!");

            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMsg("不支持的交易状态，交易返回异常!!!");
        }
    }

    /**
     * 生成二维码
     *      1.创建path（本地）文件夹File对象
     *      2.判断path文件夹File是否存在
     *          不存在：则创建path路径的文件夹
     *      3.构建付款二维码的存储路径字符串（本地）
     *      4.构建付款二维码的文件名
     *      5.调用ZxingUtils工具类生成支付二维码，并保存在path路径下（本地）
     *      6.创建付款二维码File文件对象targetQr
     *      7.调用FTPUtil工具类，上传path路径下的付款二维码到ftp服务器的img目录下
     *      8.构建ftp服务器中付款二维码的访问url
     *      9.将qrUrl：qrUrl键值对放入Map中
     *      10.返回响应值Map对象
     * @param path          付款二维码存储路径（本地）
     * @param resultMap     响应值Map对象
     * @param response      支付宝成功处理响应数据
     * @return
     */
    private ServerResponse makeQr(String path, Map<String, String> resultMap,AlipayTradePrecreateResponse response) {
        File folder = new File(path);
        if(!folder.exists()){
            folder.setWritable(true);
            folder.mkdirs();
        }
        //path：XXXX/XXX/upload+"/qr-%s.png"+orderNo(注意：要添加“/”)%s为占位符
        String qrPath = String.format(path+"/qr-%s.png", response.getOutTradeNo());
        logger.info("filePath:" + qrPath);
        String qrName = String.format("qr-%s.png", response.getOutTradeNo());
        ZxingUtils.getQRCodeImge(response.getQrCode(),256,qrPath);

        //创建付款二维码File文件对象targetQr（本地付款二维码的文件对象）
        File targetQr = new File(path,qrName);
        try {
            FTPUtil.uploadFile(Lists.newArrayList(targetQr));
            targetQr.delete();
        } catch (IOException e) {
            logger.error("上传二维码异常",e);
        }

        String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetQr.getName();
        resultMap.put("qrUrl",qrUrl);
        return ServerResponse.createBySuccess(resultMap);
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }

    /*
                            后台模块
     */
    /**
     * 业务1：获取订单列表
     *      1.使用PageHelper设置分页参数
     *      2.调用DAO层，获取所有的Order对象的List列表
     *      3.调用assembleOrderVoList(null,orderList)方法，组装OrderVo对象的List列表
     *      4.使用PageInfo开始对Order对象的List列表进行分页
     *      5.使用OrderVoList替换分页结果中的OrderList
     *      6.返回分页结果
     * @param pageNum   第几页，默认第1页
     * @param pageSize  分页大小，每页显示多少条订单信息，默认10
     * @return
     */
    @Override
    public ServerResponse manageOrderList(Integer pageNum,Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList =orderMapper.selectAll();
        List<OrderVo> orderVoList = assembleOrderVoList(null,orderList);
        PageInfo resultPage = new PageInfo(orderList);
        resultPage.setList(orderVoList);
        return ServerResponse.createBySuccess(resultPage);
    }

    /**
     * 业务2：获取订单详情
     *      1.调用DAO层接口，根据订单号获取订单Order对象
     *      2.判断订单Order是否不为空
     *          是：1.调用DAO层接口，根据订单号获取OrderItem对象的List列表
     *              2.调用assembleOrderVo(order,orderItemList)方法，组装OrderVo对象
     *              3.返回OrderVo对象给前端
     *      3.返回没有此订单错误信息
     * @param orderNo   订单号
     * @return
     */
    @Override
    public ServerResponse manageOrderDetail(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order !=null){
            List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
            OrderVo orderVo = assembleOrderVo(order,orderItemList);
            return ServerResponse.createBySuccess(orderVo);
        }
        return ServerResponse.createByErrorMsg("没有此订单");
    }

    /**
     * 业务3：订单查询：按订单号精确查询，后期可扩展为按关键字进行模糊查询
     *      1.使用PageHelper进行分页参数设置
     *      2.调用DAO层接口，根据订单号获取订单Order对象
     *      3.判断订单Order是否不为空
     *          是：1.调用DAO层接口，根据订单号获取所有订单条目OrderItem的List列表
     *              2.调用assembleOrderVo(order,orderItemList)方法，组装OrderVo对象
     *              3.使用PageInfo开始对orderLsit进行分页
     *              4.使用OrderVoList替换分页结果中的orderList
     *              5.返回分页结果
     *      4.返回无此订单错误信息
     * @param orderNo   订单号
     * @param pageNum   第几页，默认第1页
     * @param pageSize  分页大小，每页可以显示多少条订单信息，默认10
     * @return
     */
    @Override
    public ServerResponse manageOrderSearch(Long orderNo,Integer pageNum,Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order !=null){
            List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
            OrderVo orderVo = assembleOrderVo(order,orderItemList);
            PageInfo resultPage = new PageInfo(Lists.newArrayList(order));
            resultPage.setList(Lists.newArrayList(orderVo));
            return ServerResponse.createBySuccess(resultPage);
        }
        return ServerResponse.createByErrorMsg("没有此订单");
    }

    /**
     * 业务4：发货
     *      1.调用DAO层接口，根据订单号获取订单Order对象
     *      2.判断订单Order是否不为空
     *          是：1.判断订单状态值是否等于订单支付状态值
     *                  是：1.创建新的订单Order对象，用于更新订单状态
     *                      2.设置订单Order对象的属性值
     *                      3.调用DAO层接口，更新订单Order的状态
     *                      4.返回发货成功信息给前端
     *                  否：返回未付款错误信息
     *      3.返回无此订单错误信息
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse manageOrderSendGoods(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order !=null){
            if(order.getStatus() == OrderStatus.PAID.getCode()){
                Order updateOrder = new Order();
                updateOrder.setId(order.getId());
                //设置订单Order为发货状态
                updateOrder.setStatus(OrderStatus.SHIPPED.getCode());
                //设置订单Order发货时间
                updateOrder.setSendTime(new Date());
                orderMapper.updateByPrimaryKeySelective(updateOrder);
                return ServerResponse.createBySuccessMsg("发货成功");
            }else{
                return ServerResponse.createByErrorMsg("未付款，请先付款");
            }
        }
        return ServerResponse.createByErrorMsg("无此订单");
    }
}
