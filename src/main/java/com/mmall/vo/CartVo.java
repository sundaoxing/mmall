package com.mmall.vo;

import java.math.BigDecimal;
import java.util.List;
/*
        购物车Vo
            vo层，对CartProductVo的进一步封装，承载购物车对象
 */
public class CartVo {
    private List<CartProductVo> cartProductVoList;//购物车条目列表
    private BigDecimal cartTotalPrice;//购物车总价格
    private Boolean allChecked;//是否全部勾选
    private String imageHost;//商品图片的url前缀

    public List<CartProductVo> getCartProductVoList() {
        return cartProductVoList;
    }

    public void setCartProductVoList(List<CartProductVo> cartProductVoList) {
        this.cartProductVoList = cartProductVoList;
    }

    public BigDecimal getCartTotalPrice() {
        return cartTotalPrice;
    }

    public void setCartTotalPrice(BigDecimal cartTotalPrice) {
        this.cartTotalPrice = cartTotalPrice;
    }

    public Boolean getAllChecked() {
        return allChecked;
    }

    public void setAllChecked(Boolean allChecked) {
        this.allChecked = allChecked;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }
}
