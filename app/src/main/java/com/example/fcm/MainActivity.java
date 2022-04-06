package com.example.fcm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import org.eclipse.paho.android.service.MqttAndroidClient;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private WebView preload;
    private static final String TAG = MainActivity.class.getSimpleName();
    public static String token = "";
    MqttAndroidClient client;
    private BroadcastReceiver mNetworkReceiver;
    public static ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent mainService = new Intent(MainActivity.this, MainService.class);

        startService(mainService);
        loadPage();
        mNetworkReceiver = new MyBroadcastReceiver();
        registerNetworkBroadcastForNougat();

    }

    private void registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    protected void unregisterNetworkChanges() {
        try {
            unregisterReceiver(mNetworkReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public static void dialog(boolean value, Context context){
        if (token == "onPageFinished") {
            if(value){
                progressDialog.dismiss();
            }else {
                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Waiting for network...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterNetworkChanges();
    }

    void loadPage() {
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
                    }
                });
            }
        });

        webView.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(
                        Uri.parse(url));

                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "app-release.apk");
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(getApplicationContext(), "Downloading File", //To notify the Client that the file is being downloaded
                        Toast.LENGTH_LONG).show();

            }
        });
        permission();
        CookieSyncManager.createInstance(this);

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
                    token = "onPageFinished";
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

    private void permission() {

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

    public static boolean checkInternetConnection(Context context) {

        ConnectivityManager con_manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return (con_manager.getActiveNetworkInfo() != null
                && con_manager.getActiveNetworkInfo().isAvailable()
                && con_manager.getActiveNetworkInfo().isConnected());
    }

}