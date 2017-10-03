// UtilCommon.java

package com.example.android.TRY_US;

import android.app.Application;

public class UtilCommon extends Application {

    private static final String TAG = "UtilCommon";
    private boolean mGlobal;  // boolean型のグローバル変数

    /**
     * アプリケーションの起動時に呼び出される
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mGlobal = false;
    }

    /**
     * アプリケーション終了時に呼び出される
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
        mGlobal = false;
    }

    /**
     * グローバル変数の値を変更
     * @param global 変更する値
     */
    public void setGlobal(boolean global) {
        mGlobal = global;
    }

    /**
     * グローバル変数の値を取得
     * @return グローバル変数（mGlobal）
     */
    public boolean getGlobal() {
        return mGlobal;
    }

}