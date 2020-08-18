package com.gyobeom29.hipboard.activity;

public class WritePostImageViewTag {

    int index;
    String path;

    public WritePostImageViewTag(int index, String path) {
        this.index = index;
        this.path = path;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
