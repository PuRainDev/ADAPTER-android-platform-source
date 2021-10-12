package com.purain.adapterapp;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.rewarded.RewardedAd;


/*

Stores all main data of an application

 */
public class AppSingletone {
    private static AppSingletone instance;
    private static Context mContext;

    //static data section
    private static String appname = "";
    private static String admob_APPLICATION_ID = "";


    //status section
    private String FullscreenMode="full";
    private boolean Debug = false;

    //section for admob variables
    private AdView admobBANNER;
    private InterstitialAd admobInterstitial;
    private RewardedAd admobRewarded;

    private AppSingletone(Context mContext) {
        appname = mContext.getString(R.string.app_name);
        admob_APPLICATION_ID = mContext.getString(R.string.admob_APPLICATION_ID);
        Debug = Boolean.parseBoolean(mContext.getString(R.string.adapter_debug));
        this.mContext = mContext;
    }

    public static AppSingletone getInstance(Context mContext) {
        if (instance == null) {
            instance = new AppSingletone(mContext);
        }
        return instance;
    }

    public boolean isDebug() {
        return Debug;
    }

    public void setDebug(boolean debug) {
        Debug = debug;
    }

    public static String getAppname() {
        return appname;
    }

    public static String getAdmob_APPLICATION_ID() {
        return admob_APPLICATION_ID;
    }

    public String getFullscreenMode() {
        return FullscreenMode;
    }

    public void setFullscreenMode(String fullscreenMode) {
        FullscreenMode = fullscreenMode;
    }

    public AdView getAdmobBANNER() {
        return admobBANNER;
    }

    public void setAdmobBANNER(AdView admobBANNER) {
        this.admobBANNER = admobBANNER;
    }

    public InterstitialAd getAdmobInterstitial() {
        return admobInterstitial;
    }

    public void setAdmobInterstitial(InterstitialAd admobInterstitial) {
        this.admobInterstitial = admobInterstitial;
    }

    public RewardedAd getAdmobRewarded() {
        return admobRewarded;
    }

    public void setAdmobRewarded(RewardedAd admobRewarded) {
        this.admobRewarded = admobRewarded;
    }
}