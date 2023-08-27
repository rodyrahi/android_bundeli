package com.example.bundelikissan.ui.logout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import androidx.fragment.app.Fragment;

import com.example.bundelikissan.ui.home.HomeFragment;
import com.kamingo.bundelikissan.HomeActivity;
import com.kamingo.bundelikissan.databinding.FragmentHomeBinding;
import com.kamingo.bundelikissan.databinding.FragmentLogoutBinding;

public class LogoutFragment extends Fragment {

    private FragmentHomeBinding binding;
    private WebView webView;
    private ProgressBar progressBar;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        webView = binding.idHomeWebView;
        progressBar = binding.HomeProgress;

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);




        // Set up your WebViewClient
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);


            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                CookieSyncManager.getInstance().sync();

                if (getActivity() instanceof HomeActivity) {

                    Log.d("LogoutFragment", "onPageFinished: " + url);
                    if (url.contains("logout")) {
                        // Show the bottom navigation bar
                        ((HomeActivity) getActivity()).showBottomNavigationView();
                    } else {
                        // Hide the bottom navigation bar
                        ((HomeActivity) getActivity()).hideBottomNavigationView();
                    }
                }

            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                new Handler().postDelayed(() -> {
                    view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.kamingo.bundelikissan")));
                }, 60000); // 5 seconds
            }

        });

        // Load your URL
        String initialUrl = "https://bundeli.hellosugar.io/logout";
        webView.loadUrl(initialUrl);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        webView.setOnKeyListener((view, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
                    webView.goBack();
                    return true;
                }
            }
            return false;
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
