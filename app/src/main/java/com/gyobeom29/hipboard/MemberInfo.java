package com.gyobeom29.hipboard;

public class MemberInfo {

    private String name;

    private String phone;

    private String birth;

    private String address;

    private String photoUrl;


    public MemberInfo(String name , String phone , String birth , String address, String photoUrl){
        this.name = name;
        this.phone = phone;
        this.birth = birth;
        this.address = address;
        this.photoUrl = photoUrl;
    }

    public MemberInfo(String name , String phone , String birth , String address){
        this.name = name;
        this.phone = phone;
        this.birth = birth;
        this.address = address;
    }


    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }

    public void setPhone(String phone){
        this.phone = phone;
    }
    public String getPhone(){
        return phone;
    }

    public void setBirth(String birth){
        this.birth = birth;
    }
    public String getBirth(){
        return birth;
    }

    public void setAddress(String address){
        this.address = address;
    }
    public String getAddress(){
        return address;
    }

    public void setPhotoUrl(String photoUrl){this.photoUrl = photoUrl;}
    public String getPhotoUrl(){return photoUrl;}


}
