package com.gyobeom29.hipboard;


import java.util.Date;
import java.util.List;

public class PostInfo {

    private String documentId;
    private String title;
    private List<String> contents;
    private String publisher;
    private long views;
    private long likeCount;
    private Date createAt;

    public PostInfo(String title, List<String> contents, String publisher, long views, long likeCount, Date createAt) {
        this.title = title;
        this.contents = contents;
        this.publisher = publisher;
        this.views = views;
        this.likeCount = likeCount;
        this.createAt = createAt;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getContents() {
        return contents;
    }

    public String getPublisher() {
        return publisher;
    }

    public long getViews() {
        return views;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContents(List<String> contents) {
        this.contents = contents;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    @Override
    public String toString() {
        return "PostInfo{" +
                "title='" + title + '\'' +
                ", contents=" + contents +
                ", publisher='" + publisher + '\'' +
                ", views=" + views +
                ", likeCount=" + likeCount +
                ", createAt=" + createAt +
                '}';
    }
}
