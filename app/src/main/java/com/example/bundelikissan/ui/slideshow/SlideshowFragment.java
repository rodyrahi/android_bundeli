package com.example.bundelikissan.ui.slideshow;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.bundelikissan.ui.home.HomeFragment;
import com.kamingo.bundelikissan.R;
import com.kamingo.bundelikissan.databinding.FragmentHomeBinding;
import com.kamingo.bundelikissan.databinding.FragmentSlideshowBinding;

public class SlideshowFragment extends Fragment {

//    private SwipeRefreshLayout swipeRefreshLayout;
    private FragmentHomeBinding binding;
    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "MySharedPref";
    private static final String URL_KEY = "home";

    private WebView webView;
    private ProgressBar progressBar;

    // File upload variables
    private ValueCallback<Uri[]> fileUploadCallback;
    private String fileUploadCallbackName;

    private final ActivityResultLauncher<Intent> fileUploadLauncher = registerForActivityResult(
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        sharedPreferences = requireContext().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        webView = binding.idHomeWebView;
        progressBar = binding.HomeProgress;

        // Retrieve the saved URL
        String savedUrl = sharedPreferences.getString(URL_KEY, null);

        // Load the saved URL or a default URL
        String initialUrl = savedUrl != null ? savedUrl : "https://bundeli.hellosugar.io/";
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
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
//                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url != null && (url.startsWith("http://bundeli.hellosugar.io") || url.startsWith("https://bundeli.hellosugar.io"))) {
                    // External link, open in external browser
                    view.loadUrl(url);
                    return true;
                } else {
                    // Internal link or other scheme, load within WebView
                    view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                }
            }
        });

//        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);
//        swipeRefreshLayout.setOnRefreshListener(() -> webView.reload());

        webView.setOnKeyListener((view, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
                    webView.goBack();
                    return true;
                }
            }
            return false;
        });

        // Save the URL when the WebView loads a new page
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                // Save the URL
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(URL_KEY, url);
                editor.apply();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
//                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // File upload handling
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (fileUploadCallback != null) {
                    fileUploadCallback.onReceiveValue(null);
                }
                fileUploadCallback = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
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

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
