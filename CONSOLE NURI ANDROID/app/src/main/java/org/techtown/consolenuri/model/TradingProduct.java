package org.techtown.consolenuri.model;

//거래중인 제품에 관한 정보를 나타내주는 처리를 한다.
public class TradingProduct {

    String product_id;
    String productname;
    String created_at;
    String price;
    String description;
    String thumbnail;
    String category; // 카테고리 부분이 추가 되어짐.
    String writer;
    String productprgress;
    String sell_user_id; // 판매자아이디
    String buy_user_id; // 구매자아이디
    String tradingproduct_id;
    String shippingInfo; // 배송정보


    public String getShippingInfo() {
        return shippingInfo;
    }

    public void setShippingInfo(String shippingInfo) {
        this.shippingInfo = shippingInfo;
    }


    public String getBuy_user_id() {
        return buy_user_id;
    }

    public void setBuy_user_id(String buy_user_id) {
        this.buy_user_id = buy_user_id;
    }



    public String getSell_user_id() {
        return sell_user_id;
    }

    public void setSell_user_id(String sell_user_id) {
        this.sell_user_id = sell_user_id;
    }

    public String getTradingproduct_id() {
        return tradingproduct_id;
    }

    public void setTradingproduct_id(String tradingproduct_id) {
        this.tradingproduct_id = tradingproduct_id;
    }


    public String getProductprgress() {
        return productprgress;
    }

    public void setProductprgress(String productprgress) {
        this.productprgress = productprgress;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public TradingProduct(){
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getProductname() {
        return productname;
    }

    public void setProductname(String productname) {
        this.productname = productname;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

}
