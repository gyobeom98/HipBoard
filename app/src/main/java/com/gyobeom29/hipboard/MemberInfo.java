package com.gyobeom29.hipboard;

import android.os.Parcel;
import android.os.Parcelable;

public class MemberInfo implements Parcelable {

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


    protected MemberInfo(Parcel in) {
        name = in.readString();
        phone = in.readString();
        birth = in.readString();
        address = in.readString();
        photoUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeString(birth);
        dest.writeString(address);
        dest.writeString(photoUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MemberInfo> CREATOR = new Creator<MemberInfo>() {
        @Override
        public MemberInfo createFromParcel(Parcel in) {
            return new MemberInfo(in);
        }

        @Override
        public MemberInfo[] newArray(int size) {
            return new MemberInfo[size];
        }
    };

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
