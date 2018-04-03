package com.example.newbiechen.ireader.model.bean;

public class Announcement {

    private Long id;

    private String content;

    public Announcement() {
    }

    public Announcement(String content) {
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Announcement{" +
                "id=" + id +
                ", content='" + content + '\'' +
                '}';
    }
}