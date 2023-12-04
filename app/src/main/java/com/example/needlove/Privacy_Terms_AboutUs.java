package com.example.needlove;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.needlove.utils.CheckConnection;

public class Privacy_Terms_AboutUs extends AppCompatActivity {

    LinearLayout progress;
    TextView txtType;
    ImageView imgType;
    WebView webView;
    LinearLayout lnrError;
    String URLlink=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy__terms__about_us);

        progress = findViewById(R.id.progress);
        txtType = findViewById(R.id.txtType);
        imgType = findViewById(R.id.imgType);
        webView = findViewById(R.id.webView);
        lnrError = findViewById(R.id.lnrError);



        String witch = getIntent().getExtras().getString("witch");

        if (witch.equals("privacy"))
        {

            txtType.setText("Privacy Policy");
            imgType.setImageResource(R.drawable.prof_privacy);
            URLlink ="https://2doz.net/privacy-policy.html";
            getData(URLlink);
        }
        else if (witch.equals("terms"))
        {
            txtType.setText("Terms and Conditions");
            imgType.setImageResource(R.drawable.prof_terms);
            URLlink ="https://2doz.net/terms.html";
            getData(URLlink);
        }
        else {
            txtType.setText("About us");
            imgType.setImageResource(R.drawable.prof_about);
            URLlink ="https://2doz.net/about.html";
            getData(URLlink);
        }


        webView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                progress.setVisibility(View.GONE);
            }

        });


    }

    public void back(View view) {
        finish();
    }


    void getData(String URL)
    {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new Callback());

        if (CheckConnection.checkInternetConnection(Privacy_Terms_AboutUs.this))
        {
            webView.loadUrl(URL);
        }
        else {
            progress.setVisibility(View.GONE);
            webView.setVisibility(View.GONE);
            lnrError.setVisibility(View.VISIBLE);
        }

    }

    public void RefreshConect(View view) {
        lnrError.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);

        if (CheckConnection.checkInternetConnection(Privacy_Terms_AboutUs.this))
        {
            webView.loadUrl(URLlink);
        }
        else {
            progress.setVisibility(View.GONE);
        }



    }

    public class Callback extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progress.setVisibility(View.GONE);
        }

    }
}
