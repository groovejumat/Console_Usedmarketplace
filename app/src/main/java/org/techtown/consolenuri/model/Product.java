package org.techtown.consolenuri.model;

import java.io.Serializable;

public class Product implements Serializable {
    String product_id;
    String productname;
    String created_at;
    String price;
    String description;
    String thumbnail;
    String category; // 카테고리 부분이 추가 되어짐.
    String writer;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }



    public Product(String product_id, String productname, String created_at, String price, String description, String thumbnail, String writer) {
        this.product_id = product_id;
        this.productname = productname;
        this.created_at = created_at;
        this.price = price;
        this.description = description;
        this.thumbnail = thumbnail;
        this.writer = writer;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }



    public Product(){
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
