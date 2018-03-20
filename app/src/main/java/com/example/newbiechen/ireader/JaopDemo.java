package com.example.newbiechen.ireader;

import android.util.Log;

import com.tamic.rx.fastdown.content.DownLoadInfo;

import jaop.domain.MethodBodyHook;
import jaop.domain.annotation.Jaop;
import jaop.domain.annotation.Replace;

@Jaop  //配置文件的开关
public class JaopDemo {
    private static final String TAG = "JaopDemo";

    @Replace("com.tamic.rx.fastdown.http.DownOkHttpHandler.get")  // hook 掉onCreate 方法的方法体
    public void replace1(MethodBodyHook hook) {
        DownLoadInfo downLoadInfo = (DownLoadInfo) hook.getArgs()[0];
        int range = Integer.parseInt(downLoadInfo.mHeaders.get("Range"));
        downLoadInfo.mHeaders.remove("Range");
//        downLoadInfo.mHeaders.put("Range", "bytes=" + range + "-" + (range + 1000));
        try {
            hook.process(new Object[]{downLoadInfo, hook.getArgs()[1]});
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        Log.i(TAG, "replace1: ");
    }

}