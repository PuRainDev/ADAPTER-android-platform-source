package com.purain.adapterapp;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.onesignal.OneSignal;

import static android.graphics.Color.parseColor;

/*

Uses JavascriptInterface to make connection between native and web code

Todo:
 - move functionality to their own classes

 */

public class ADAPTERBridge {
    Context mContext;
    Activity activity;
    WebView mWebView;
    AppSingletone mAppSingletone;

    /** Instantiate the interface and set the context */
    ADAPTERBridge(Context c, Activity a, WebView w) {
        mContext = c;
        activity = a;
        mWebView = w;
        mAppSingletone = AppSingletone.getInstance(mContext);
    }

    public void updatereward() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:(function f() {" +
                        "if (c2_callFunction)\n" +
                        "    c2_callFunction(\"onAdmobRewarded\");" +
                        "})()");
                mWebView.loadUrl("javascript:(function f() {" +
                        "if (c3_callFunction)\n" +
                        "    c3_callFunction(\"onAdmobRewarded\");" +
                        "})()");
            }
        }, 1000);

    }

    @JavascriptInterface
    public void setOrientation(String orientation) {
        final String orientationLower = orientation.toLowerCase();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch(orientationLower) {
                    case "landscape":
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        break;
                    case "portrait":
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        break;
                    case "unspecified":
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                        break;
                    default:
                        if (mAppSingletone.isDebug()) {
                            Toast toast = Toast.makeText(mContext, "Unknown orientation value", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                }
            }
        });
    }

    @JavascriptInterface
    public void onesignalInit(String ID) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                OneSignal.initWithContext(mContext);
                OneSignal.setAppId(ID);
            }
        });
    }

    @JavascriptInterface
    public void admobBannerInit(String ID, String size) {
        final String sizeLower = size.toLowerCase();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinearLayout layout = (LinearLayout) activity.findViewById(R.id.adBannerContainer);
                mAppSingletone.setAdmobBANNER(new AdView(mContext));

                switch(sizeLower) {
                    case "banner":
                        mAppSingletone.getAdmobBANNER().setAdSize(AdSize.BANNER);
                        break;
                    case "large_banner":
                        mAppSingletone.getAdmobBANNER().setAdSize(AdSize.LARGE_BANNER);
                        break;
                    case "medium_rectangle":
                        mAppSingletone.getAdmobBANNER().setAdSize(AdSize.MEDIUM_RECTANGLE);
                        break;
                    default:
                        if (mAppSingletone.isDebug()) {
                            Toast toast = Toast.makeText(activity, "Unknown banner size value", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                }
                mAppSingletone.getAdmobBANNER().setAdUnitId(ID);
                layout.addView(mAppSingletone.getAdmobBANNER());
            }
        });
    }

    @JavascriptInterface
    public void admobBannerLoad() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AdRequest adRequest = new AdRequest.Builder().build();
                mAppSingletone.getAdmobBANNER().loadAd(adRequest);
            }
        });
    }

    @JavascriptInterface
    public void setStatusbarColor(String hexcolor) {
        Window window = activity.getWindow();
        window.setStatusBarColor(parseColor(hexcolor));
    }

    @JavascriptInterface
    public void admobInterestialLoad(String ID) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AdRequest adRequest = new AdRequest.Builder().build();

                InterstitialAd.load(activity,ID, adRequest,
                        new InterstitialAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                // The mInterstitialAd reference will be null until
                                // an ad is loaded.
                                mAppSingletone.setAdmobInterstitial(interstitialAd);
                                mWebView.loadUrl("javascript:(function f() {" +
                                        "if (c2_callFunction)\n" +
                                        "    c2_callFunction(\"onAdmobInterestialLoaded\");"+
                                        "})()");
                                mWebView.loadUrl("javascript:(function f() {" +
                                        "if (c3_callFunction)\n" +
                                        "    c3_callFunction(\"onAdmobInterestialLoaded\");"+
                                        "})()");
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                if (mAppSingletone.isDebug()) {
                                    Toast toast = Toast.makeText(mContext, "Cant load interestial", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                                mAppSingletone.setAdmobInterstitial(null);
                            }
                        });
            }
        });
    }

    @JavascriptInterface
    public void admobInterestialShow() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mAppSingletone.getAdmobInterstitial() != null) {
                    mAppSingletone.getAdmobInterstitial().show(activity);
                }
            }
        });
    }

    @JavascriptInterface
    public void admobRewardedLoad(String ID) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AdRequest adRequest = new AdRequest.Builder().build();

                RewardedAd.load(mContext, ID,
                        adRequest, new RewardedAdLoadCallback() {
                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                if (mAppSingletone.isDebug()) {
                                    Toast toast = Toast.makeText(activity, "Cant load reward", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                                mAppSingletone.setAdmobRewarded(null);
                            }

                            @Override
                            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                                mAppSingletone.setAdmobRewarded(rewardedAd);
                                mWebView.loadUrl("javascript:(function f() {" +
                                        "if (c2_callFunction)\n" +
                                        "    c2_callFunction(\"onAdmobRewardedLoaded\");"+
                                        "})()");
                                mWebView.loadUrl("javascript:(function f() {" +
                                        "if (c3_callFunction)\n" +
                                        "    c3_callFunction(\"onAdmobRewardedLoaded\");"+
                                        "})()");

                            }
                        });

            }
        });
    }

    @JavascriptInterface
    public void admobRewardedShow() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mAppSingletone.getAdmobRewarded() != null) {
                    mAppSingletone.getAdmobRewarded() .show(activity, new OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                            int rewardAmount = rewardItem.getAmount();
                            String rewardType = rewardItem.getType();
                            updatereward();
                        }
                    });
                }
            }
        });
    }

    @JavascriptInterface
    public void setFullscreen(String mode) {
        final String modeLower = mode.toLowerCase();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View decorView = activity.getWindow().getDecorView();
                switch(modeLower) {
                    case "full":
                        decorView.setSystemUiVisibility(
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        break;
                    case "partially":
                        decorView.setSystemUiVisibility(
                                View.SYSTEM_UI_FLAG_VISIBLE);
                        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        break;
                    case "off":
                        decorView.setSystemUiVisibility(
                                View.SYSTEM_UI_FLAG_VISIBLE);
                        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        break;
                    default:
                        if (mAppSingletone.isDebug()) {
                            Toast toast = Toast.makeText(activity, "Unknown fullscreen mode", Toast.LENGTH_SHORT);
                            toast.show();
                            return;
                        }
                }

                mAppSingletone.setFullscreenMode(modeLower);

            }
        });
    }


    @JavascriptInterface
    public void exit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.finishAndRemoveTask();
        } else {
            activity.finish();
        }
    }
}


