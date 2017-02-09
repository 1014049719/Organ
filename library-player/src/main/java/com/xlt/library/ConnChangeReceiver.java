package com.xlt.library;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author zjh
 * @date 2016/4/18
 */
public class ConnChangeReceiver extends BroadcastReceiver{
    private OnNetWorkChangeListener onNetWorkChangeListener;
    public final static int WIFI = 1;

    public ConnChangeReceiver(OnNetWorkChangeListener onNetWorkChangeListener){
        this.onNetWorkChangeListener = onNetWorkChangeListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobNetInfo=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNetInfo=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mobNetInfo.isConnected() && !wifiNetInfo.isConnected()) {
            //网络不可用
            if (onNetWorkChangeListener != null){
                onNetWorkChangeListener.newWorkError();
            }
        }else {
            //网络可用
            if (onNetWorkChangeListener != null){
                int type = 0;
                if (wifiNetInfo.isConnected()){
                    type = WIFI;
                }
                onNetWorkChangeListener.netWorkSuccess(type);
            }
        }
    }

    public interface OnNetWorkChangeListener{
        void netWorkSuccess(int type);
        void newWorkError();
    }
}
