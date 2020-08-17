package com.gyobeom29.hipboard;


import java.util.Date;
import java.util.List;

public class PostInfo {

    private String title;
    private List<String> contents;
    private String publisher;
    private Date createAt;

    public PostInfo(String title, List<String> contents, String publisher, Date createAt) {
        this.title = title;
        this.contents = contents;
        this.publisher = publisher;
        this.createAt = createAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getContents() {
        return contents;
    }

    public void setContents(List<String> contents) {
        this.contents = contents;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    @Override
    public String toString() {
        return "WriteInfo{" +
                "title='" + title + '\'' +
                ", contents=" + contents +
                ", publisher='" + publisher + '\'' +
                ", createAt=" + createAt +
                '}';
    }
}
