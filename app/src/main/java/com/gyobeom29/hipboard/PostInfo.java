package com.gyobeom29.hipboard;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.List;

public class PostInfo implements Parcelable {

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

    protected PostInfo(Parcel in) {
        documentId = in.readString();
        title = in.readString();
        contents = in.createStringArrayList();
        publisher = in.readString();
        views = in.readLong();
        likeCount = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(documentId);
        dest.writeString(title);
        dest.writeStringList(contents);
        dest.writeString(publisher);
        dest.writeLong(views);
        dest.writeLong(likeCount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PostInfo> CREATOR = new Creator<PostInfo>() {
        @Override
        public PostInfo createFromParcel(Parcel in) {
            return new PostInfo(in);
        }

        @Override
        public PostInfo[] newArray(int size) {
            return new PostInfo[size];
        }
    };

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

    public void setViews(long views) {
        this.views = views;
    }

    public void setLikeCount(long likeCount) {
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
