package com.example.newbiechen.ireader.model.bean.packages;

import com.example.newbiechen.ireader.model.bean.BaseBean;

import java.util.List;

/**
 * Created by newbiechen on 17-6-2.
 */

public class SearchBookPackage extends BaseBean {

    private List<BooksBean> books;

    public List<BooksBean> getBooks() {
        return books;
    }

    public void setBooks(List<BooksBean> books) {
        this.books = books;
    }

    public static class BooksBean {
        /**
         * _id : 51d11e782de6405c45000068
         * hasCp : true
         * title : 大主宰
         * cat : 玄幻
         * author : 天蚕土豆
         * site : zhuishuvip
         * cover : /agent/http://image.cmfu.com/books/2750457/2750457.jpg
         * shortIntro : 大千世界，位面交汇，万族林立，群雄荟萃，一位位来自下位面的天之至尊，在这无尽世界，演绎着令人向往的传奇，追求着那主宰之路。 无尽火域，炎帝执掌，万火焚苍穹。 武...
         * lastChapter : 第1565章 人法合一
         * retentionRatio : 57.92
         * banned : 0
         * latelyFollower : 415890
         * wordCount : 4942027
         */

        private int id;
        private String name;
        private String url;
        private String timestamp;


        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return "BooksBean{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", url='" + url + '\'' +
                    ", timestamp='" + timestamp + '\'' +
                    '}';
        }
    }
}
