/**
 * Copyright 2015 Tamic
 *
 * @author Liuyongkui
 * <p>
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.newbiechen.ireader.utils;

import android.content.ContentValues;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.tamic.rx.fastdown.DLToastManager;
import com.tamic.rx.fastdown.R;
import com.tamic.rx.fastdown.RxConstants;
import com.tamic.rx.fastdown.client.Type;
import com.tamic.rx.fastdown.content.DownLoadInfo;
import com.tamic.rx.fastdown.content.DownLoadInfo.Status;
import com.tamic.rx.fastdown.core.RxDLController;
import com.tamic.rx.fastdown.core.RxDownLoadCenter;
import com.tamic.rx.fastdown.core.RxDownloadManager;
import com.tamic.rx.fastdown.core.Style;
import com.tamic.rx.fastdown.database.DLDatabaseManager;
import com.tamic.rx.fastdown.http.DownOkHttpHandler;
import com.tamic.rx.fastdown.http.RxCallback;
import com.tamic.rx.fastdown.http.exception.RxException;
import com.tamic.rx.fastdown.task.AbsDownLoadTask;
import com.tamic.rx.fastdown.task.DLCallbackMsg;
import com.tamic.rx.fastdown.task.DLCallbackMsg.State;
import com.tamic.rx.fastdown.util.DLResource;
import com.tamic.rx.fastdown.util.LogWraper;
import com.tamic.rx.fastdown.util.Utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.Response;
import okhttp3.ResponseBody;


/**
 * NormalTask
 */
public class DLNormalTask extends AbsDownLoadTask {

    DownhttpCallback callback;
    private boolean mIsMu38;
    int updateCount;

    public DLNormalTask(DownLoadInfo aInfo) {
        super(aInfo);
        callback = new DownhttpCallback();
        //mTaskHandler.setBufSize(BUF);
        mLastRefreshTime = 0;
        mIsMu38 = false;
        mCanContinue = true;
    }

    @Override
    public void start() {
        LogWraper.d(TAG, "start normal task " + mInfo.mFilename);
        try {
            Utils.makeDirs(mInfo.mSavepath);
            mInfo.mHeaders.clear();
            try {
                if (RxDownloadManager.getInstance().getListener().getCookie(mInfo.mUrl) != null) {
                    mInfo.addHeader("Cookie", RxDownloadManager.getInstance().getListener().getCookie(mInfo.mUrl));
                }

                if (RxDownloadManager.getInstance().getListener().getUA() != null) {
                    mInfo.addHeader("User-Agent", RxDownloadManager.getInstance().getListener().getUA());
                }

            } catch (Exception e) {
                LogWraper.d(TAG, "no cookie or ua");
            }
            LogWraper.d(TAG, "transferred bytes: " + mInfo.mDownloadedbytes);
            if (!TextUtils.isEmpty(mInfo.mReferer)) {
                mInfo.addHeader("Referer", mInfo.mReferer);
            }
            mInfo.addHeader("Range", "bytes=" + mInfo.mDownloadedbytes );
            mStarttime = System.currentTimeMillis();
            mInfo.mHostUrl = mInfo.mUrl;
            LogWraper.d(TAG, "create time: " + mInfo.mCreatedtime);
            DownOkHttpHandler.getInstance()
                    .get(mInfo, callback);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pause() {
        LogWraper.d(TAG, "pause" + mInfo.mFilename + " " + mInfo.mStatus);
        if (mInfo.mStatus == Status.RUNNING) {
            mInfo.mStatus = Status.PAUSED;
            mInfo.mSpeed = 0;
            stop();
        } else if (mInfo.mStatus == Status.READY) {
            mInfo.mStatus = Status.PAUSED;
            mInfo.mSpeed = 0;
        }
    }

    @Override
    public void cancel(boolean aIsdelfile, boolean aIsnotifyUI) {
        LogWraper.d(TAG, "cancel" + mInfo.mUrl);
        if (mInfo.mStatus == Status.RUNNING) {
            stop();
        }
        mInfo.mStatus = Status.CANCEL;
        if (aIsnotifyUI && mInfo.isImplicit != 1) {
            DLCallbackMsg msg = new DLCallbackMsg(State.CANCEL, mInfo.mKey, mInfo.mUrl,
                    mInfo.mDownloadedbytes, mInfo.mTotalbytes, mInfo.mSavepath, mInfo.mFilename, "",
                    mInfo.mSpeed, mInfo.mType);
            RxDownLoadCenter.getInstance(null).notifyUI(msg);
        }
        RxDownLoadCenter.getInstance(null).getHttpclient().cancel(mInfo, true, callback);
    }

    @Override
    public void stop() {
        LogWraper.d(TAG, "stop " + mInfo.mFilename);
        RxDownLoadCenter.getInstance(null).getHttpclient().cancel(mInfo, true, callback);
    }

    /**
     * 退出
     */
    private void exit() {
        try {
            mOut.close();
        } catch (Exception e) {
            LogWraper.d(TAG, "mOut is already closed!");
        }
        mOut = null;
    }

    /**
     * retry
     *
     * @return 成功发起重试请求返回true，否则返回false
     */
    private boolean retry() {
        LogWraper.d(TAG, "retry times left: " + mRetrytimes);
        mRetrytimes--;
        if (mCanContinue && mRetrytimes >= 0) {

            int retryinteval = 1000 * 10 * (RETRY_TIMES - mRetrytimes);
            LogWraper.d(TAG, "retry after " + retryinteval + " milliseconds");
            try {
                Thread.sleep(retryinteval);
                if (mInfo.mStatus == Status.RUNNING) {
                    start();
                }
                return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return mInfo.mStatus == Status.PAUSED;
            }
        } else {
            return false;
        }
    }

    /**
     * verify
     *
     * @return result
     */
    private boolean verify() {
        LogWraper.d(TAG, "total:" + mInfo.mTotalbytes + " downloaded:" + mInfo.mDownloadedbytes);
        if (!ism3u8() && mInfo.mTotalbytes > 0) {
            if (mInfo.mDownloadedbytes < mInfo.mTotalbytes) {
                if (mCanContinue) {
                    LogWraper.d("verify failed. retry & continue!");
                    start();
                    return false;
                } else {
                    LogWraper.d("verify failed. do not support partial, so go to onfail");
                    callback.onError(new RxException("移动网络出错"), 0 );
                    return false;
                }
            } else if (mInfo.mDownloadedbytes > mInfo.mTotalbytes * (1 + RxConstants.TOLERANT_RATE)) {
                LogWraper.d("超过100%");
                stop();
                callback.onError(new RxException("超过100%。总大小：" + mInfo.mTotalbytes + "，已下载："
                        + mInfo.mDownloadedbytes), 0);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    /**
     * 判断是否m3u8
     *
     * @return m3u8返回true，否则返回false
     */
    private boolean ism3u8() {
        return (mIsMu38 || mInfo.mFilename.contains(M3U8) || mInfo.mUrl.contains(M3U8));
    }





    private class DownhttpCallback extends RxCallback<Response> {

        public Call call;
        public int id;


        void onStart() {

            LogWraper.d(TAG, "onstart: " + mInfo.mFilename);
            mStarttime = System.currentTimeMillis();

            mBytesThistime = 0;
            if (RxDownLoadCenter.getInstance(null).isShowInUI(mInfo)) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        RxDLController.getInstance(null).notifyNewTask(mInfo);
                    }
                });
            }
            RxDownLoadCenter.getInstance(null).sendShowToastMessage(mInfo.mFilename + "正在后台下载..",
                    DLToastManager.TYPE_CLICKABLE);

           /* if (RxDownLoadCenter.getInstance(null).isShowInUI(mInfo)) {
                RxDownLoadCenter.getInstance(null).sendShowToastMessage(mInfo.mFilename + "正在后台下载..",
                        DLToastManager.TYPE_CLICKABLE);
            }*/

            DLCallbackMsg msg = new DLCallbackMsg(State.START,
                    mInfo.mKey,
                    mInfo.mUrl,
                    mInfo.mDownloadedbytes,
                    mInfo.mTotalbytes,
                    mInfo.mSavepath,
                    mInfo.mFilename, "",
                    mInfo.mSpeed,
                    mInfo.mType,
                    mInfo.mPackageName,
                    mInfo.mFrom,
                    mInfo.mDownloadType,
                    mInfo.mPosition);
            RxDownLoadCenter.getInstance(null).notifyUI(msg);

        }

        void onDownloading(byte[] bytes, int downloaded) {
            // write flie
            if (mOut == null) {
                try {
                    LogWraper.d(TAG, "new mOut");
                    mOut = new FileOutputStream(mInfo.mSavepath + mInfo.mFilename, true);

                } catch (FileNotFoundException e) {
                    LogWraper.d(TAG, "创建输出流失败");
                    stop();
                    onError(new RxException("创建输出流失败"), id);
                    return;
                }
            }
            try {

                mOut.write(bytes, 0, downloaded);
            } catch (IOException e) {
                LogWraper.d(TAG, "写文件失败" + mInfo.mFilename);
                LogWraper.d(TAG, "cause: " + e.getCause());
                LogWraper.d(TAG, "message: " + e.getMessage());
                if (e.getMessage().contains("No space left on device")) {
                    // sd卡满
                    LogWraper.d(TAG, "SD卡满了");
                    if (mInfo.isImplicit != 1 && mInfo.mAttribute.equals(Type.NORMAL)) {
                        DLToastManager.showToast(DLResource.getString(R.string.download_disksize_low), DLToastManager.TYPE_NORMAL);
                    }
                    stop();
                    onError(new RxException(RxConstants.ERROR_INSUFFICIENT_STORAGE), id);
                } else {
                    stop();
                    onError(new RxException(ERROR_FILE), id);
                }
                return;
            }

            mInfo.mDownloadedbytes += downloaded;

            if (!ism3u8() && mInfo.mTotalbytes > 0
                    && mInfo.mDownloadedbytes > mInfo.mTotalbytes * (1 + RxConstants.TOLERANT_RATE)) {
                stop();
                onError(new RxException("超过100%。总大小：" + mInfo.mTotalbytes + "，已下载：" + mInfo.mDownloadedbytes), id);
                return;
            }
            mBytesThistime += downloaded;
            long currenttime = System.currentTimeMillis();
            if (currenttime > mStarttime) {
                mInfo.mSpeed = mBytesThistime * 1000 / (currenttime - mStarttime);
            }

            final int progress = (int) (mInfo.mDownloadedbytes* 100 / mInfo.mTotalbytes);
            //update
            if (updateCount == 0 || progress >= updateCount) {
                updateCount += 1;
                RxDownLoadCenter.getInstance(null).sendUpdateProgressMessage();
                RxDownLoadCenter.getInstance(null).sendUpdateMessage(mInfo);
            }

            long currentTime = System.currentTimeMillis();
            if (currentTime - mLastRefreshTime >= REFRESH_INTEVAL) {

                mLastRefreshTime = currentTime;
                DLCallbackMsg msg = new DLCallbackMsg(State.RECEIVE, mInfo.mKey, mInfo.mUrl,
                        mInfo.mDownloadedbytes, mInfo.mTotalbytes, mInfo.mSavepath, mInfo.mFilename, "",
                        mInfo.mSpeed, mInfo.mType);
                RxDownLoadCenter.getInstance(null).notifyUI(msg);
            }
            mInfo.mHeaders.put("Range", mInfo.mDownloadedbytes + "");

            // uodate DB
            ContentValues values = new ContentValues();

            values.put(DLDatabaseManager.Columns.CURRENTBYTES, mInfo.mDownloadedbytes);
            RxDownLoadCenter.getInstance(null).getDBManager()
                    .update(values, new String[]{mInfo.mKey});

            if (currentTime - mLastRefreshTime >= REFRESH_INTEVAL) {
                // 上层回调
                mLastRefreshTime = currentTime;
                DLCallbackMsg msg = new DLCallbackMsg(State.RECEIVE,
                        mInfo.mKey,
                        mInfo.mUrl,
                        mInfo.mDownloadedbytes,
                        mInfo.mTotalbytes,
                        mInfo.mSavepath,
                        mInfo.mFilename,
                        "",
                        mInfo.mSpeed,
                        mInfo.mType,
                        mInfo.mPackageName,
                        mInfo.mFrom,
                        mInfo.mDownloadType,
                        mInfo.mPosition);
                RxDownLoadCenter.getInstance(null).notifyUI(msg);
                DLCallbackMsg msg1 = new DLCallbackMsg(State.REFRESH,
                        mInfo.mKey,
                        mInfo.mUrl,
                        mInfo.mDownloadedbytes,
                        mInfo.mTotalbytes,
                        mInfo.mSavepath,
                        mInfo.mFilename,
                        "",
                        mInfo.mSpeed,
                        mInfo.mType,
                        mInfo.mPackageName,
                        mInfo.mFrom,
                        mInfo.mDownloadType,
                        mInfo.mPosition);
                RxDownLoadCenter.getInstance(null).notifyUI(msg1);
            }



        }


        void onDownloadSucess() {
            LogWraper.d("soar", "onSuccess" + mInfo.mFilename);
            /*if (!verify()) {
                return;
            }*/
            try {
                // rename
                Utils.removeSuffix(mInfo.mSavepath, mInfo.mFilename);
                // 任务信息中改名字
                if (mInfo.mFilename.endsWith(Utils.SUFFIX)) {
                    int index = mInfo.mFilename.lastIndexOf(Utils.SUFFIX);
                    if (index >= 0) {
                        mInfo.mFilename = mInfo.mFilename.substring(0, index);
                    }
                }
                exit();
            } catch (Exception e) {
                onError(new RxException("重命名失败"), id);
                e.printStackTrace();
                return;
            }
            try {

                if (ism3u8()) {
                    mIsMu38 = false;
                    mInfo.mTotalbytes = mInfo.mDownloadedbytes * 47;
                    mInfo.mStatus = Status.READY;
                    // add new task
                    DownLoadInfo newinfo = new DownLoadInfo(mInfo);
                    newinfo.mDownloadStyle = Style.M3U8_STYLE;
                    newinfo.mAttribute = mInfo.mAttribute;
                } else {
                    // 改状态
                    mInfo.mStatus = Status.SUCCESS;
                    mInfo.mCompletetime = System.currentTimeMillis();
                    if (mInfo.mTotalbytes <= 0) {
                        mInfo.mTotalbytes = Utils.getFileLength(mInfo.mSavepath + mInfo.mFilename);
                    }
                    mInfo.mSpeed = mInfo.mTotalbytes * 1000 / (mInfo.mCompletetime - mStarttime);
                    //call back
                    DLCallbackMsg msg = new DLCallbackMsg(State.SUCCESS, mInfo.mKey, mInfo.mUrl,
                            mInfo.mDownloadedbytes, mInfo.mTotalbytes, mInfo.mSavepath, mInfo.mFilename, "",
                            mInfo.mSpeed, mInfo.mType, mInfo.mPackageName, mInfo.mFrom, mInfo.mDownloadType, mInfo.mPosition);
                    RxDownLoadCenter.getInstance(null).notifyUI(msg);
                    // 其他UI相关回调
                    if (mInfo.isImplicit != 1) {
                        // isImplicit
                        if (mInfo.mType != null
                                && (mInfo.mType.equals(Type.KERNEL)
                                || mInfo.mType.equals(Type.FRAME) || mInfo.mType
                                .equals(Type.PLUGIN))) {
                            RxDownLoadCenter.getInstance(null).getDBManager()
                                    .delete(new String[]{mInfo.mKey});
                        } else {
                            // update DB
                            ContentValues values = new ContentValues();
                            values.put(DLDatabaseManager.Columns.STATUS,
                                    Utils.status2int(mInfo.mStatus));
                            values.put(DLDatabaseManager.Columns.COMPLETETIME, mInfo.mCompletetime);
                            values.put(DLDatabaseManager.Columns.FILENAME, mInfo.mFilename);
                            RxDownLoadCenter.getInstance(null).getDBManager()
                                    .update(values, new String[]{mInfo.mKey});
                        }
                        if (RxDownLoadCenter.getInstance(null).isShowInUI(mInfo)) {
                            // update Notifilation
                            RxDownLoadCenter.getInstance(null).sendUpdateResultMessage(mInfo);
                            //  // update finsh
                            RxDownLoadCenter.getInstance(null).sendSuccessMessage(mInfo);
                            // show toast
                            if (!mInfo.mType.equals(Type.NOVEL) &&
                                    !mInfo.mType.equals(Type.FRAME)) {
                                String toastContent = mInfo.mRealName + DLResource.getString(R.string.download_files_completed);
                                RxDownLoadCenter.getInstance(null).sendShowToastMessage(toastContent,
                                        DLToastManager.TYPE_NORMAL);
                            }
                        }

                        //若是图片
                        if (mInfo.mFilename.endsWith("bmp")
                                || mInfo.mFilename.endsWith("jpeg")
                                || mInfo.mFilename.endsWith("jpg")
                                || mInfo.mFilename.endsWith("png")) {
                        }
                    } else {
                        RxDownLoadCenter.getInstance(null).getDBManager().delete(new String[]{mInfo.mKey});
                    }
                }
            } catch (Exception e) {
                LogWraper.e(e.getMessage());
                onError(new RxException(DLResource.getString(R.string.download_unknow_error)), id);
                e.printStackTrace();
                return;
            }
        }


        public void onGetResponse(Response response) {
            //print some log
            Headers headers = response.headers();

            for (String tmp : headers.names()) {
                LogWraper.d(TAG, tmp + ":" + headers.get(tmp));
            }
            // disposition
            String contentdisposition = headers.get("Content-Disposition");
            if (contentdisposition != null) {
                if (contentdisposition.contains(M3U8)) {
                    mIsMu38 = true;
                }
                LogWraper.d(TAG, Utils.getFilename(contentdisposition, mInfo.mUrl));
            }
            // add suffix
            if (!Utils.hasExtension(mInfo.mFilename)) {
                String oldname;
                String metutype = headers.get("Content-Type");
                if (metutype != null) {
                    if (metutype.contains("png")) {
                        oldname = mInfo.mFilename;
                        if (mInfo.mFilename.endsWith(Utils.SUFFIX)) {
                            int index = mInfo.mFilename.lastIndexOf(Utils.SUFFIX);
                            if (index >= 0) {
                                mInfo.mFilename = mInfo.mFilename.substring(0, index);
                            }
                        }
                        mInfo.mFilename += ".png";
                        mInfo.mFilename = Utils.getUniqueFilename(mInfo.mSavepath, mInfo.mFilename);
                        mInfo.mFilename = Utils.addSuffix(mInfo.mFilename);
                        Utils.renameTo(mInfo.mSavepath, oldname, mInfo.mFilename);
                    } else if (metutype.contains("jpg") || metutype.contains("jpeg")) {
                        oldname = mInfo.mFilename;
                        if (mInfo.mFilename.endsWith(Utils.SUFFIX)) {
                            int index = mInfo.mFilename.lastIndexOf(Utils.SUFFIX);
                            if (index >= 0) {
                                mInfo.mFilename = mInfo.mFilename.substring(0, index);
                            }
                        }
                        mInfo.mFilename += ".jpg";
                        mInfo.mFilename = Utils.getUniqueFilename(mInfo.mSavepath, mInfo.mFilename);
                        mInfo.mFilename = Utils.addSuffix(mInfo.mFilename);
                        Utils.renameTo(mInfo.mSavepath, oldname, mInfo.mFilename);
                    }
                }
            }
           /* // 断点续传
            int code = response.code();
            LogWraper.d(TAG, "response code: " + code);
            if (code == 200) {
                LogWraper.d(TAG, "不支持断点");
                mCanContinue = false;
                // reSet
                mInfo.mDownloadedbytes = 0;
                // delete download Flie
                Utils.deleteFile(mInfo.mSavepath + mInfo.mFilename);
                // update DB
                ContentValues values = new ContentValues();
                values.put(DLDatabaseManager.Columns.CURRENTBYTES, mInfo.mDownloadedbytes);
                RxDownLoadCenter.getInstance(null).getDBManager().update(values, new String[]{mInfo.mKey});
            } else if (code == 206) {
                mCanContinue = true;
            }*/
        }


        @Override
        public void onNextResponse(Call call, Response response, int id) {
            this.call =call;
            this.id = id;
            dispathDownResponse(call, response, id);
        }

        private void dispathDownResponse(Call call, Response response, int id) {
            //getHeader
            onGetResponse(response);
            // start
            onStart();

            ResponseBody body = response.body();

            long fileSize = body.contentLength();

            mInfo.mTotalbytes = fileSize;
            InputStream inputStream = null;

            //
            try {
                int read = 0;
                byte[] fileReader = new byte[4096];
                inputStream = body.byteStream();
                while (true) {
                    read = inputStream.read(fileReader);
                    if (read == -1) {
                        break;
                    }
                    onDownloading(fileReader, read);
                }

                /*if (mInfo.mTotalbytes == mInfo.mDownloadedbytes) {

                }*/
                onDownloadSucess();
            } catch (IOException e) {
                if (e instanceof java.net.SocketException) {
                    LogWraper.e(e.getMessage());
                    downloadCancel();
                } else {
                    e.printStackTrace();
                    LogWraper.e(e.getMessage());
                    onError(new RxException("read io error"), id);
                }

            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }


        @Override
        public Response onHandleResponse(Response response, int id) throws Exception {
            return response;
        }

        @Override
        public void onError(RxException e, int id) {
            LogWraper.d(TAG, "onError " + mInfo.mFilename);
            LogWraper.d(TAG, "reason: " + e.getMessage());
            if (e.getMessage().toString().contains("ConnectException") && retry()) {
                return;
            }
            exit();
            mInfo.mStatus = Status.FAIL;
            mInfo.mCompletetime = System.currentTimeMillis();
            if (mInfo.isImplicit != 1) {
                // update db
                ContentValues values = new ContentValues();
                values.put(DLDatabaseManager.Columns.STATUS, Utils.status2int(mInfo.mStatus));
                values.put(DLDatabaseManager.Columns.COMPLETETIME, mInfo.mCompletetime);
                values.put(DLDatabaseManager.Columns.FILENAME, mInfo.mFilename);
                RxDownLoadCenter.getInstance(null).getDBManager().update(values, new String[]{mInfo.mKey});
                // call back
                if (RxDownLoadCenter.getInstance(null).isShowInUI(mInfo)) {
                    // 更新通知栏
                    RxDownLoadCenter.getInstance(null).sendUpdateResultMessage(mInfo);
                    String reason = e.getMessage();
                    String toastContent;
                    if (reason == null || !reason.equals(RxConstants.ERROR_INSUFFICIENT_STORAGE)) {
                        toastContent = mInfo.mRealName
                                + RxDownloadManager.getInstance().getContext().getResources()
                                .getString(R.string.download_error);
                    } else {
                        toastContent = mInfo.mRealName
                                + RxDownloadManager.getInstance().getContext().getResources()
                                .getString(R.string.download_error) + ERROR_SDCARD_FULL;
                    }
                    RxDownLoadCenter.getInstance(null).sendShowToastMessage(toastContent,
                            DLToastManager.TYPE_NORMAL);
                    RxDownLoadCenter.getInstance(null).sendUpdateMessage(mInfo);
                }
            } else {
                //  delete db
                RxDownLoadCenter.getInstance(null).getDBManager().delete(new String[]{mInfo.mKey});
            }
            DLCallbackMsg msg = new DLCallbackMsg(State.FAIL, mInfo.mKey, mInfo.mUrl,
                    mInfo.mDownloadedbytes, mInfo.mTotalbytes, mInfo.mSavepath, mInfo.mFilename,
                    e.getMessage(), mInfo.mSpeed, mInfo.mType);
            RxDownLoadCenter.getInstance(null).notifyUI(msg);
        }

        @Override
        public void onCancel(RxException e, int id) {
            downloadCancel();
        }

    }

    private void downloadCancel() {

        if(mInfo.isImplicit != 1) {
            DLToastManager.showToast(mInfo.mFilename + "已取消下载");
        }

        DLCallbackMsg msg1 = new DLCallbackMsg(State.CANCEL,
                mInfo.mKey,
                mInfo.mUrl,
                mInfo.mDownloadedbytes,
                mInfo.mTotalbytes,
                mInfo.mSavepath,
                mInfo.mFilename,
                "",
                mInfo.mSpeed,
                mInfo.mType,
                mInfo.mPackageName,
                mInfo.mFrom,
                mInfo.mDownloadType,
                mInfo.mPosition);
        RxDownLoadCenter.getInstance(null).notifyUI(msg1);
    }

}
