package org.techtown.consolenuri.model;

import java.io.Serializable;

public class Address implements Serializable {
    String name; //받는 사람의 성함.
    String contact;
    String address; //api로 검색한 우편번호.
    String addressdetail; //상세주소.
    String requset; //배송요청.

    public Address(){}; //기본 생성자 완성.


    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddressdetail() {
        return addressdetail;
    }

    public void setAddressdetail(String addressdetail) {
        this.addressdetail = addressdetail;
    }

    public String getRequset() {
        return requset;
    }

    public void setRequset(String requset) {
        this.requset = requset;
    }
}
