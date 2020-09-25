package com.gyobeom29.hipboard.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.gyobeom29.hipboard.R;

public class DetailNewsActivity extends NoActiveBasicActivity {

    WebView newsWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_news);
        newsWebView = findViewById(R.id.detail_news_web_view);
        Intent intent = getIntent();
        showHome();
        if(intent == null) finish();
        else{
            String link = intent.getStringExtra("link");
            newsWebView.setWebChromeClient(new WebChromeClient());
            newsWebView.setWebViewClient(new WebViewClient());
            newsWebView.getSettings().setJavaScriptEnabled(true);
            newsWebView.loadUrl(link);
        }

    }
}