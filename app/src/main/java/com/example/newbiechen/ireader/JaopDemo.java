package com.example.newbiechen.ireader;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.tamic.rx.fastdown.content.DownLoadInfo;

import jaop.domain.MethodBodyHook;
import jaop.domain.MethodCallHook;
import jaop.domain.annotation.Jaop;
import jaop.domain.annotation.Replace;

@Jaop  //配置文件的开关
public class JaopDemo {
    private static final String TAG = "JaopDemo";

    @Replace("com.tamic.rx.fastdown.http.DownOkHttpHandler.get")  // hook 掉onCreate 方法的方法体
    public void replace1(MethodBodyHook hook) {
        DownLoadInfo downLoadInfo = (DownLoadInfo) hook.getArgs()[0];
        downLoadInfo.mHeaders.remove("Range");
        downLoadInfo.mHeaders.put("Range", "bytes=0-1");
        try {
            hook.process(new Object[]{downLoadInfo, hook.getArgs()[1]});
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        Log.i(TAG, "replace1: ");
    }

    @Replace("android.widget.Toast.makeText") // hook Toast makeText 方法的调用处, 替换toast的文本
    public void replace2(MethodCallHook hook) {
        Object[] args = hook.getArgs();
        hook.setResult(Toast.makeText((Context) args[0], "hook toast", Toast.LENGTH_LONG)); // 设置返回值
    }
}