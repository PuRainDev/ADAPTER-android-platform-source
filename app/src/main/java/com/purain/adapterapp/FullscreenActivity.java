package com.purain.adapterapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebViewAssetLoader;
import static android.graphics.Color.parseColor;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.onesignal.OneSignal;

import java.io.IOException;

public class FullscreenActivity extends AppCompatActivity {

    private View mContentView;
    private WebView mWebView;
    private WebViewAssetLoader assetLoader;
    private ImageView imgPreloader;

    //status section
    private boolean fullscreen=true;

    //section for admob variables
    private AdView admobBANNER;
    private InterstitialAd admobInterstitial;
    private RewardedAd admobRewarded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_fullscreen);
        mContentView = findViewById(R.id.preloader);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        imgPreloader = (ImageView) findViewById(R.id.preloader);

        mWebView = (WebView) findViewById(R.id.mWebView);
        assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Glide.with(this)
                .load(R.drawable.adapterloading)
                .into(imgPreloader);

        mWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setAllowContentAccess(true);
        mWebView.getSettings().setSupportMultipleWindows(true);
        mWebView.getSettings().setDefaultTextEncodingName("utf-8");
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setDatabaseEnabled(true);
        mWebView.setLongClickable(false);

        mWebView.setHapticFeedbackEnabled(false);
        mWebView.addJavascriptInterface(new WebAppInterface(this, FullscreenActivity.this), "ADAPTER");



            mWebView.setWebViewClient(new WebViewClient() {

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view,
                                                                  WebResourceRequest request) {
                    Log.e("tesst", String.valueOf(request.getUrl()));
                    if (String.valueOf(request.getUrl()).endsWith(".js")) {
                        try {
                            return new WebResourceResponse("text/javascript", "UTF-8", getAssets().open(String.valueOf(request.getUrl()).replace("https://appassets.androidplatform.net/assets/","")));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return assetLoader.shouldInterceptRequest(request.getUrl());
                }
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.startsWith("tel:")) {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                        startActivity(intent);
                        return true;
                    } else if (url.startsWith("file:///android_asset/www/index.html")) {
                        view.loadUrl(url);
                        return true;
                    } else if (url.startsWith("https://appassets.androidplatform.net/")) {
                        view.loadUrl(url);
                        return true;
                    } else if (url.startsWith("javascript")) {
                        view.loadUrl(url);
                        return true;
                    } else {
                        view.getContext().startActivity(
                                new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        return true;
                    }
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    mWebView.setVisibility(View.VISIBLE);
                }
            });

            mWebView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                    return super.onJsAlert(view, url, message, result);
                }
            });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("https://appassets.androidplatform.net/assets/www/index.html");
            }
        }, 1000);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
    }



    public class WebAppInterface {

        Context mContext;
        Activity activity;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c, Activity a) {
            mContext = c;
            activity = a;
        }

        @JavascriptInterface
        public void setOrientation(String orientation) {
            final String orientationLower = orientation.toLowerCase();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch(orientationLower) {
                        case "landscape":
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            break;
                        case "portrait":
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            break;
                        case "unspecified":
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                            break;
                        default:
                            Toast toast = Toast.makeText(FullscreenActivity.this, "Unknown orientation value", Toast.LENGTH_SHORT);
                            toast.show();
                    }
                }
            });
        }

        @JavascriptInterface
        public void onesignalInit(String ID) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    OneSignal.initWithContext(FullscreenActivity.this);
                    OneSignal.setAppId(ID);
                }
            });
        }

        @JavascriptInterface
        public void admobBannerInit(String ID, String size) {
            final String sizeLower = size.toLowerCase();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LinearLayout layout = (LinearLayout) findViewById(R.id.adBannerContainer);
                    admobBANNER = new AdView(FullscreenActivity.this);

                    switch(sizeLower) {
                        case "banner":
                            admobBANNER.setAdSize(AdSize.BANNER);
                            break;
                        case "large_banner":
                            admobBANNER.setAdSize(AdSize.LARGE_BANNER);
                            break;
                        case "medium_rectangle":
                            admobBANNER.setAdSize(AdSize.MEDIUM_RECTANGLE);
                            break;
                        default:
                            Toast toast = Toast.makeText(FullscreenActivity.this, "Unknown banner size value", Toast.LENGTH_SHORT);
                            toast.show();
                    }
                    admobBANNER.setAdUnitId(ID);
                    layout.addView(admobBANNER);
                }
            });
        }

        @JavascriptInterface
        public void admobBannerLoad() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AdRequest adRequest = new AdRequest.Builder().build();
                    admobBANNER.loadAd(adRequest);
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AdRequest adRequest = new AdRequest.Builder().build();

                    InterstitialAd.load(FullscreenActivity.this,ID, adRequest,
                            new InterstitialAdLoadCallback() {
                                @Override
                                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                    // The mInterstitialAd reference will be null until
                                    // an ad is loaded.
                                    admobInterstitial = interstitialAd;
                                    mWebView.loadUrl("javascript:(function f() {" +
                                            "if (c2_callFunction)\n" +
                                            "    c2_callFunction(\"onAdmobInterestialLoaded\");"+
                                            "})()");
                                }

                                @Override
                                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                    // Handle the error
                                    Toast toast = Toast.makeText(FullscreenActivity.this, "Cant load interestial", Toast.LENGTH_SHORT);
                                    toast.show();
                                    admobInterstitial = null;
                                }
                            });
                }
            });
        }

        @JavascriptInterface
        public void admobInterestialShow() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (admobInterstitial != null) {
                        admobInterstitial.show(FullscreenActivity.this);
                    }
                }
            });
        }

        @JavascriptInterface
        public void admobRewardedLoad(String ID) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AdRequest adRequest = new AdRequest.Builder().build();

                    RewardedAd.load(FullscreenActivity.this, ID,
                            adRequest, new RewardedAdLoadCallback() {
                                @Override
                                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                    Toast toast = Toast.makeText(FullscreenActivity.this, "Cant load reward", Toast.LENGTH_SHORT);
                                    toast.show();
                                    admobRewarded = null;
                                }

                                @Override
                                public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                                    admobRewarded = rewardedAd;;
                                    mWebView.loadUrl("javascript:(function f() {" +
                                            "if (c2_callFunction)\n" +
                                            "    c2_callFunction(\"onAdmobRewardedLoaded\");"+
                                            "})()");

                                }
                            });

                }
            });
        }

        @JavascriptInterface
        public void admobRewardedShow() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (admobRewarded != null) {
                        Activity activityContext = FullscreenActivity.this;
                        admobRewarded.show(activityContext, new OnUserEarnedRewardListener() {
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
        public void setFullscreenOff() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fullscreen = false;
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

                }
            });
        }

        @JavascriptInterface
        public void setFullscreenOn() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fullscreen = true;
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                }
            });
        }


        @JavascriptInterface
        public void exit() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask();
            } else {
                finish();
            }
        }
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
            }
        }, 1000);

    }

    @Override
    protected void onDestroy() {
        /*if (mWebView != null)
            mWebView.destroy();*/
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (fullscreen) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}

