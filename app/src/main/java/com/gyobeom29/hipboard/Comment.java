package com.gyobeom29.hipboard;

import java.util.Date;

public class Comment {

    private String commentId;
    private String content;
    private String writer;
    private boolean isPostWriter;
    private Date writeDate;

    public Comment(String content, String writer, boolean isPostWriter, Date writeDate) {
        this.content = content;
        this.writer = writer;
        this.isPostWriter = isPostWriter;
        this.writeDate = writeDate;
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

    @Override
    public String toString() {
        return "Comment{" +
                "commentId='" + commentId + '\'' +
                ", content='" + content + '\'' +
                ", writer='" + writer + '\'' +
                ", isPostWriter=" + isPostWriter +
                ", writeDate=" + writeDate +
                '}';
    }
}
