package com.example.newbiechen.ireader.model.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by zohar on 2018/3/29.
 * desc:
 */
@Entity
public class BookMarkerBean {

    @Id(autoincrement = true)
    private Long id;

    private String bookId;

    private int curChapterPos;

    @Generated(hash = 1147587871)
    public BookMarkerBean(Long id, String bookId, int curChapterPos) {
        this.id = id;
        this.bookId = bookId;
        this.curChapterPos = curChapterPos;
    }

    @Generated(hash = 734546185)
    public BookMarkerBean() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public int getCurChapterPos() {
        return curChapterPos;
    }

    public void setCurChapterPos(int curChapterPos) {
        this.curChapterPos = curChapterPos;
    }

    @Override
    public String toString() {
        return "BookMarkerBean{" +
                "id='" + id + '\'' +
                ", bookId='" + bookId + '\'' +
                ", curChapterPos=" + curChapterPos +
                '}';
    }
}
