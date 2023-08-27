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
import android.util.Log;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.bundelikissan.ui.home.HomeFragment;
import com.kamingo.bundelikissan.R;
import com.kamingo.bundelikissan.databinding.FragmentHomeBinding;
import com.kamingo.bundelikissan.databinding.FragmentSlideshowBinding;

public class SlideshowFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "MySharedPref";
    private static final String URL_KEY = "home";

    private WebView webView;
    private ProgressBar progressBar;

    // File upload variables
    private ValueCallback<Uri[]> fileUploadCallback;
    private String fileUploadCallbackName;

    private HomeFragment.BottomNavViewCallback bottomNavViewCallback;

    public interface BottomNavViewCallback {
        void showBottomNavigationView();
        void hideBottomNavigationView();
    }

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



        bottomNavViewCallback = (HomeFragment.BottomNavViewCallback) getActivity();


        sharedPreferences = requireContext().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        webView = binding.idHomeWebView;
        progressBar = binding.HomeProgress;

        // Retrieve the saved URL
        String savedUrl = sharedPreferences.getString(URL_KEY, null);

        // Load the saved URL or a default URL
        String initialUrl = savedUrl != null ? savedUrl : "https://bundeli.hellosugar.io/video";
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
                if (url.contains("home")) {
                    Log.d("WebViewDebug", "Show bottom navigation");
                    bottomNavViewCallback.showBottomNavigationView();
                } else {
                    Log.d("WebViewDebug", "Hide bottom navigation");
                    bottomNavViewCallback.hideBottomNavigationView();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                CookieSyncManager.getInstance().sync();
//                swipeRefreshLayout.setRefreshing(false);
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


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        webView.setWebViewClient(new WebViewClient() {
            private static final int ERROR_TIMEOUT = 60000; // 5 seconds

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                // Handle the error here
                // For example, load a local error page
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.kamingo.bundelikissan")));
                    }
                }, ERROR_TIMEOUT);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
//                swipeRefreshLayout.setRefreshing(false);
            }
        });





        // Save the URL when the WebView loads a new page



        // File upload handling
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

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}