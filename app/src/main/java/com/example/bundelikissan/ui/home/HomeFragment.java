package com.example.bundelikissan.ui.home;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.kamingo.bundelikissan.R;
import com.kamingo.bundelikissan.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "MySharedPref";
    private static final String URL_KEY = "home";

    private WebView webView;
    private ProgressBar progressBar;

    // File upload variables
    private ValueCallback<Uri[]> fileUploadCallback;
    private String fileUploadCallbackName;

    private BottomNavViewCallback bottomNavViewCallback;
    private ActivityResultLauncher<Intent> fileUploadLauncher;

    public interface BottomNavViewCallback {
        void showBottomNavigationView();
        void hideBottomNavigationView();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        bottomNavViewCallback = (BottomNavViewCallback) getActivity();

        sharedPreferences = requireContext().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        webView = binding.idHomeWebView;
        progressBar = binding.HomeProgress;

        // Retrieve the saved URL
        String savedUrl = sharedPreferences.getString(URL_KEY, null);

        // Load the saved URL or a default URL
        String initialUrl = savedUrl != null ? savedUrl : "https://bundeli.kamingo.in/home";
        webView.loadUrl(initialUrl);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Enable file access
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);

        // Show progress bar when the page starts loading
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                bottomNavViewCallback.hideBottomNavigationView();
//                if (url.contains("home") || url.contains("chat") || url.contains("userprofile") || url.contains("mandi") || url.contains("query")) {
//                    // Show the bottom navigation bar
//                    bottomNavViewCallback.showBottomNavigationView();
//                } else {
//                    // Hide the bottom navigation bar
//                    bottomNavViewCallback.hideBottomNavigationView();
//                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                CookieSyncManager.getInstance().sync();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url != null && (url.startsWith("https://bundeli.hellosugar.io") || url.startsWith("https://bundeli.hellosugar.io"))) {
//                     External link, open in external browser
                    view.loadUrl(url);
                    return true;
                } else {
                    // Internal link or other scheme, load within WebView
                    view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                }
            }

        });

        webView.setOnKeyListener((view, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
                    webView.goBack();
                    return true;
                }
            }
            return false;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (fileUploadCallback != null) {
                    fileUploadCallback.onReceiveValue(null);
                }
                fileUploadCallback = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Allow multiple file selection
                fileUploadCallbackName = fileChooserParams.getFilenameHint();

                try {
                    fileUploadLauncher.launch(intent);
                } catch (ActivityNotFoundException e) {
                    fileUploadCallback = null;
                    Toast.makeText(getContext(), "File upload not supported", Toast.LENGTH_SHORT).show();
                    return false;
                }

                return true;
            }
        });

        // Create the file upload launcher
        fileUploadLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && fileUploadCallback != null) {
                        Uri[] resultUris = null;
                        Intent data = result.getData();
                        if (data != null) {
                            if (data.getClipData() != null) {
                                int count = data.getClipData().getItemCount();
                                resultUris = new Uri[count];
                                for (int i = 0; i < count; i++) {
                                    resultUris[i] = data.getClipData().getItemAt(i).getUri();
                                }
                            } else if (data.getData() != null) {
                                resultUris = new Uri[]{data.getData()};
                            }
                        }
                        fileUploadCallback.onReceiveValue(resultUris);
                        fileUploadCallback = null;
                    } else {
                        if (fileUploadCallback != null) {
                            fileUploadCallback.onReceiveValue(null);
                            fileUploadCallback = null;
                        }
                    }
                }
        );

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
