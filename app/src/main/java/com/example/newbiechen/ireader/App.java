package com.example.newbiechen.ireader;

import android.app.Application;
import android.content.Context;

import com.tamic.fastdownsimple.down.DownloadInit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 * Created by newbiechen on 17-4-15.
 */

public class App extends Application {
    private static Context sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
//        startService(new Intent(getContext(), DownloadService.class));
        DownloadInit.init(getApplicationContext());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        hookPackageClassLoader(base, new MyClassLoader());
    }

    public static Context getContext(){
        return sInstance;
    }

    private static boolean hookPackageClassLoader(Context context, ClassLoader appClassLoaderNew) {
        try {
            Field packageInfoField = Class.forName("android.app.ContextImpl").getDeclaredField("mPackageInfo");
            packageInfoField.setAccessible(true);
            Object loadedApkObject = packageInfoField.get(context);
            Class<?> LoadedApkClass = Class.forName("android.app.LoadedApk");
            Method getClassLoaderMethod = LoadedApkClass.getDeclaredMethod("getClassLoader");
            ClassLoader appClassLoaderOld = (ClassLoader) getClassLoaderMethod.invoke(loadedApkObject);
            Field appClassLoaderField = LoadedApkClass.getDeclaredField("mClassLoader");
            appClassLoaderField.setAccessible(true);
            appClassLoaderField.set(loadedApkObject, appClassLoaderNew);
            return true;
        } catch (Throwable ignored) {
            ignored.printStackTrace();
        }
        return false;
    }
}