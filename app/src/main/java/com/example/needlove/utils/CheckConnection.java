package com.example.needlove.utils;

import android.content.Context;
import android.net.ConnectivityManager;

public class CheckConnection {


    public static boolean checkInternetConnection(Context context)
    {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return (connectivityManager.getActiveNetworkInfo() !=null
                && connectivityManager.getActiveNetworkInfo().isAvailable()
                && connectivityManager.getActiveNetworkInfo().isConnected());

    }
}
