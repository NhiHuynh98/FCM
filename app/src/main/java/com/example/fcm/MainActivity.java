package com.example.fcm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
//import com.google.firebase.messaging.FirebaseMessaging;


public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private WebView preload;
    private static final String TAG = MainActivity.class.getSimpleName();
    public static String token = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        preload =(WebView)findViewById(R.id.preload);
        preload.loadUrl("file:///android_asset/loading.html");
        // webview use to call own site
        webView =(WebView)findViewById(R.id.webView);

        webView.setWebViewClient(new MyWebClient());
        webView.loadUrl("https://chat.chek.agency/users/sign_up");
        webView .getSettings().setJavaScriptEnabled(true);
        webView .getSettings().setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient() {
            // Grant permissions for cam
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                Log.d(TAG, "onPermissionRequest");
                MainActivity.this.runOnUiThread(new Runnable() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void run() {
                        request.grant(request.getResources());
//                        Log.d(TAG, request.getOrigin().toString());
//                        if(request.getOrigin().toString().equals("https://webcamera.io/")) {
//                            Log.d(TAG, "GRANTED");
//                            request.grant(request.getResources());
//                        } else {
//                            Log.d(TAG, "DENIED");
//                            request.deny();
//                        }
                    }
                });
            }


        });
        permission();
        CookieSyncManager.createInstance(this);

//        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
//
//            @Override
//            public void onComplete(@NonNull Task<String> task) {
//                token = task.getResult();
//                Log.d(TAG, "Token:" + token);
//
//                CookieManager cookieManager = CookieManager.getInstance();
//                cookieManager.setAcceptCookie(true);
//
//                cookieManager.setCookie("https://chat.chek.agency", "firebase="+ token);
//                CookieSyncManager.getInstance().sync();
//
//                String cookie = cookieManager.getCookie("https://chat.chek.agency");
//                Log.d(TAG, "cookie ------>"+cookie);
//                Log.d(TAG, "token ------>"+token);
//            }
//        });

    }

    @Override
    public void onBackPressed() {
        webView.goBack();
    }


    public class MyWebClient extends WebViewClient
    {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    preload.setVisibility(View.GONE);
                }
            }, 1000);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;

        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            view.loadUrl("file:///android_asset/error.html");
        }

    }

    void permission() {

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "You already granted the permission camera ------>");
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "You already granted the permission audio ------>");
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 100);
        }
    }
}