package com.example.newbiechen.ireader;

import android.util.Log;

public class MyClassLoader extends ClassLoader {
    private static final String TAG = "MyClassLoader";
    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        if (className.equals("com.tamic.rx.fastdown.task.DLNormalTask")) {
            className = "com.example.newbiechen.ireader.utils.DLNormalTask";
            Log.i(TAG, "loadClass: ");
        }
        return super.loadClass(className);
    }
}