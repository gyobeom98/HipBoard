package com.gyobeom29.hipboard;

import java.util.Date;

public class Comment {

    private String commentId;
    private String content;
    private String writePublisher;
    private boolean isPostWriter;
    private Date writeDate;
    private String writer;

    public Comment(String content, String writer, boolean isPostWriter, Date writeDate, String writePublisher) {
        this.content = content;
        this.writer = writer;
        this.isPostWriter = isPostWriter;
        this.writeDate = writeDate;
        this.writePublisher = writePublisher;
    }

    public String getContent() {
        return content;
    }

    public String getWriter() {
        return writer;
    }

    public boolean isPostWriter() {
        return isPostWriter;
    }

    public Date getWriteDate() {
        return writeDate;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getCommentId() {
        return commentId;
    }

    public String getWritePublisher() {
        return writePublisher;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "commentId='" + commentId + '\'' +
                ", content='" + content + '\'' +
                ", writePublisher='" + writePublisher + '\'' +
                ", isPostWriter=" + isPostWriter +
                ", writeDate=" + writeDate +
                ", writer='" + writer + '\'' +
                '}';
    }
}
