package com.example.newbiechen.ireader.ui.activity;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.newbiechen.ireader.R;
import com.example.newbiechen.ireader.model.bean.BookMarkerBean;
import com.example.newbiechen.ireader.model.local.BookRepository;
import com.example.newbiechen.ireader.ui.base.BaseActivity;

import java.util.List;

public class BookMarkerActivity extends BaseActivity {

    protected RecyclerView rcvBookMarker;

    @Override
    protected int getContentId() {
        return R.layout.activity_book_marker;
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        getSupportActionBar().setTitle("书签列表");
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        String id = getIntent().getStringExtra("id");
        initView();
        List<BookMarkerBean> bookMarkers = BookRepository.getInstance().getBookMarkers(id);
        rcvBookMarker.setLayoutManager(new LinearLayoutManager(this));
        BookMarkerAdapter adapter = new BookMarkerAdapter(bookMarkers);
        rcvBookMarker.setAdapter(adapter);
        adapter.setOnItemClickListener((adapter1, view, position) -> {
            setResult(bookMarkers.get(position).getCurChapterPos());
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void initView() {
        rcvBookMarker = (RecyclerView) findViewById(R.id.rcv_book_marker);
    }

    class BookMarkerAdapter extends BaseQuickAdapter<BookMarkerBean, BaseViewHolder> {

        public BookMarkerAdapter(List<BookMarkerBean> data) {
            super(R.layout.item_book_marker, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, BookMarkerBean item) {
            helper.setText(R.id.tv_content, "第" + (item.getCurChapterPos() + 1) + "页");
        }
    }

}
