package com.example.newbiechen.ireader.ui.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.newbiechen.ireader.R;
import com.example.newbiechen.ireader.ui.base.BaseActivity;
import com.tamic.fastdownsimple.widget.DownloadAdapter;
import com.tamic.fastdownsimple.widget.PracticalRecyclerView;
import com.tamic.rx.fastdown.client.Type;
import com.tamic.rx.fastdown.content.DownLoadInfo;
import com.tamic.rx.fastdown.core.RxDownLoadCenter;
import com.tamic.rx.fastdown.core.RxDownloadManager;
import com.tamic.rx.fastdown.listener.IUIDownHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * DownLoadList
 * Created by Tamic on 2016-12-27.
 */
public class NewDownLoadListActivity extends BaseActivity implements IUIDownHandler {
    @BindView(R.id.content_main)
    RelativeLayout mContentMain;
    @BindView(R.id.recycler)
    PracticalRecyclerView mRecycler;
    DownloadAdapter mAdapter;

    List<DownLoadInfo> data = new ArrayList<>();
    HashSet<DownLoadInfo> set = new HashSet<>();
    @BindView(R.id.tab_tl_indicator)
    TabLayout tabTlIndicator;
//    @BindView(R.id.toolbar)
//    Toolbar toolbar;

    @Override
    protected int getContentId() {
        return R.layout.activity_dowmload_list;
    }

    @Override
    protected void initWidget() {
        super.initWidget();

        ButterKnife.bind(this, this);

        mAdapter = new DownloadAdapter();

        RxDownloadManager.getInstance().setUiDownHandler(this);
        RxDownLoadCenter.getInstance(this).loadTask();

        //mAdapter = DownApplication.downloadAdapter;
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.setAdapterWithLoading(mAdapter);
        // Data references.
        List<DownLoadInfo> allinfo = RxDownLoadCenter.getInstance(this).getAllInfoWithType(Type.NORMAL);
        List<DownLoadInfo> allSuccessinfo = RxDownLoadCenter.getInstance(this).getAllSuccessInfo();

        if (!data.containsAll(allinfo)) {
            data.addAll(allinfo);
        }

        if (data.addAll(allSuccessinfo)) {
            data.addAll(allSuccessinfo);
        }

        mAdapter.getData().clear();
        mAdapter.addAll(data);
        tabTlIndicator.setVisibility(View.GONE);
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        getSupportActionBar().setTitle("下载列表");
    }


    @Override
    public void notifyComplete(DownLoadInfo info) {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void notifyRefresh(DownLoadInfo downLoadInfo) {
        mAdapter.notifyDataSetChanged();

    }

    @Override
    public void notifyRefresh() {

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void notifyNewTask(DownLoadInfo info) {
        // checkEmpty();
        RxDownloadManager.getInstance().loadTask();
        loadData(null);
        mAdapter.notifyDataSetChanged();

    }

    public void loadData(String tag) {
        List<DownLoadInfo> infos = RxDownLoadCenter.getInstance(this).getAllInfoWithType(Type.NORMAL);
        data.clear();
        for (DownLoadInfo info : infos) {
            data.add(info);
        }
        mAdapter.clearData();
        mAdapter.addAll(data);
        sort();
    }

    private void sort() {
        Collections.sort(data, new Comparator<DownLoadInfo>() {
            @Override
            public int compare(DownLoadInfo item1, DownLoadInfo item2) {
                return (int) (item2.mCreatedtime - item1.mCreatedtime);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }



    /*private void unsubscribe() {
        List<DownLoadInfo> list = mAdapter.getData();
        for (DownLoadInfo each : list) {
            each.unsubscrbe();
        }
    }*/
}
