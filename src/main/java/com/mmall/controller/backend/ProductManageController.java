package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
/*
            后台模块：商品接口
 */
@Controller
@RequestMapping("/manage/product")
public class ProductManageController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IProductService iProductService;
    @Autowired
    private IFileService iFileService;

    /**
     * 管理员操作：添加/更新商品
     *              1.从session中获取当前登陆User对象
     *              2.判断User对象是否为空
     *              3.调用service层接口，校验该User对象的角色是否为管理员
     *                  是：调用service层接口，将商品Product对象添加到数据库中
     *              4.返回无权限信息给前端
     * @param session   Session对象
     * @param product   商品对象
     * @return
     */
    @RequestMapping(value = "add_or_update_product.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> addOrUpdateProduct(HttpSession session,Product product){
        User user =(User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iProductService.addOrUpdateProduct(product);
        }
        return ServerResponse.createByErrorMsg("无权限,请用管理员");
    }

    /**
     * 管理员操作：设置商品销售状态
     *              1.从session中获取当前登陆User对象
     *              2.判断User对象是否为空
     *              3.调用service层接口，校验该User对象的角色是否为管理员
     *                  是：调用service层接口，更新商品Id，商品status到数据库中
     *              4.返回无权限信息给前端
     * @param session       Session对象
     * @param productId     商品Id
     * @param status        商品在售状态
     * @return
     */
    @RequestMapping(value = "set_sale_status.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> setSaleStatus(HttpSession session,Integer productId,Integer status){
        User user =(User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iProductService.setSaleStatus(productId,status);
        }
        return ServerResponse.createByErrorMsg("无权限,请用管理员");
    }

    /**
     * 管理员操作：获取商品详情
     *              1.从session中获取当前登陆User对象
     *              2.判断User对象是否为空
     *              3.调用service层接口，校验该User对象的角色是否为管理员
     *                  是：调用service层接口，获取商品详情
     *              4.返回无权限信息给前端
     * @param session       Session对象
     * @param productId     商品Id
     * @return
     */
    @RequestMapping(value = "get_product_detail.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<ProductDetailVo> getProductDetail(HttpSession session , Integer productId){
        User user =(User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iProductService.getProductDetailManage(productId);
        }
        return ServerResponse.createByErrorMsg("无权限,请用管理员");
    }

    /**
     * 管理员操作：获取商品列表
     *              1.从session中获取当前登陆User对象
     *              2.判断User对象是否为空
     *              3.调用service层接口，校验该User对象的角色是否为管理员
     *                  是：调用service层接口，获取商品列表
     *              4.返回无权限信息给前端
     * @param session   Session对象
     * @param pageNum   第几页，默认第1页
     * @param pageSize  每页大小：每页包含多少条商品信息，默认值10
     * @return
     */
    @RequestMapping(value = "get_product_list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> getProductList(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                                   @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize){
        User user =(User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iProductService.getProductList(pageNum,pageSize);
        }
        return ServerResponse.createByErrorMsg("无权限,请用管理员");
    }

    /**
     * 管理员操作：按照商品名（模糊查询）/商品ID查询商品列表
     *              1.从session中获取当前登陆User对象
     *              2.判断User对象是否为空
     *              3.调用service层接口，校验该User对象的角色是否为管理员
     *                  是：调用service层接口，更新商品Id，商品status到数据库中
     *              4.返回无权限信息给前端
     * @param session       Session对象
     * @param productName   商品名称
     * @param productId     商品Id
     * @param pageNum       第几页，默认第1页
     * @param pageSize      每页大小：每页包含多少条商品信息，默认值10
     * @return
     */
    @RequestMapping(value = "search_product_list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> searchProductList(HttpSession session,String productName,Integer productId
            , @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum
            , @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize){
        User user =(User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iProductService.searchProductList(productName,productId,pageNum,pageSize);
        }
        return ServerResponse.createByErrorMsg("无权限,请用管理员");
    }

    /**
     * 管理员操作：上传文件
     *          1.从session中获取当前登陆User对象
     *              2.判断User对象是否为空
     *              3.调用service层接口，校验该User对象的角色是否为管理员
     *                  否：返回无权限信息给前端
     *                  是：1.获取当前上下文环境的路径path
     *                      2.调用file服务层接口，上传文件
     *                      3.将上传结果返回给前端
     *                      4.构造响应数据Map对象：uri+url
     *              4.返回无权限信息给前端
     * @param file      MultipartFile类型的文件
     * @param request   HttpServletRequest请求对象
     * @return
     */
    @RequestMapping(value = "upload.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Map> upload(HttpSession session,@RequestParam(value = "upload_file",required = false) MultipartFile file,
                                      HttpServletRequest request){
        User user =(User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            String path =request.getSession().getServletContext().getRealPath("upload");
            String targetFileName =iFileService.upload(file,path);
            if(StringUtils.isBlank(targetFileName)){
                return ServerResponse.createByErrorMsg("上传失败");
            }
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            Map fileMap = new HashMap();
            fileMap.put("uri",targetFileName);
            fileMap.put("url",url);
            return ServerResponse.createBySuccess(fileMap);
        }
        return ServerResponse.createByErrorMsg("无权限,请用管理员");
    }

    /**
     * 管理员操作：富文本上传
     *              1.从session中获取当前登陆User对象
     *              2.判断User对象是否为空
     *              3.调用service层接口，校验该User对象的角色是否为管理员
     *                  否：返回无权限信息给前端
     *                  是：1.获取当前上下文环境的路径path
     *                      2.调用file服务层接口，上传文件
     *                      3.将上传结果返回给前端
     *                      4.构造响应数据Map对象：success+msg+file_path
     * @param session       Session对象
     * @param file          MultipartFile类型的文件
     * @param request       HttpServletRequest请求对象
     * @param response      HttpServletResponse响应对象
     * @return
     */
    @RequestMapping(value = "rich_text_img_upload.do",method = RequestMethod.POST)
    @ResponseBody
    public Map richTextImgUpload(HttpSession session, @RequestParam(value = "upload_rich_text",required = false) MultipartFile file,
                                 HttpServletRequest request, HttpServletResponse response){
        Map resultMap = new HashMap();
        User user =(User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            resultMap.put("success",false);
            resultMap.put("msg","请登陆管理员");
            return resultMap;
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            String path =request.getSession().getServletContext().getRealPath("upload");
            String targetFileName =iFileService.upload(file,path);
            if (StringUtils.isBlank(targetFileName)){
                resultMap.put("success",false);
                resultMap.put("msg","上传失败");
                return resultMap;
            }

            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            resultMap.put("success",true);
            resultMap.put("msg","上传成功");
            resultMap.put("file_path",url);
            response.addHeader("Access-Control-Allow-Headers","X-File-Name");
            return resultMap;
        }
        else{
            resultMap.put("success",false);
            resultMap.put("msg","无权限操作");
            return resultMap;
        }
    }
}
