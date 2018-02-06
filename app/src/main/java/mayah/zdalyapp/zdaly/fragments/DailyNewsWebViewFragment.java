package mayah.zdalyapp.zdaly.fragments;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mayah.zdalyapp.zdaly.R;

public class DailyNewsWebViewFragment extends Fragment {

    @BindView(R.id.webView)
    WebView webView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    static JSONObject newsDict;

    public DailyNewsWebViewFragment() {
        // Required empty public constructor
    }


    public static DailyNewsWebViewFragment newInstance(JSONObject object) {
        newsDict = object;
        DailyNewsWebViewFragment fragment = new DailyNewsWebViewFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily_news_web_view, container, false);
        ButterKnife.bind(this, view);

        String websiteUrl = newsDict.optString("LINK", "");

        if (!(websiteUrl.equals(""))) {

            webView.setWebViewClient(new myWebClient());
            webView.getSettings().setJavaScriptEnabled(true);

            webView.getSettings().setSupportZoom(true);       //Zoom Control on web (You don't need this
            //if ROM supports Multi-Touch
            webView.getSettings().setBuiltInZoomControls(true); //Enable Multitouch if supported by ROM
            webView.setBackgroundResource(R.color.white);
            webView.getSettings().setUseWideViewPort(true);
            webView.getSettings().setLoadWithOverviewMode(false);


            webView.loadUrl(websiteUrl);
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @OnClick(R.id.btnPrev)
    public void onPrevious() {
        FragmentManager fm = getActivity().getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.remove(this);
        fragmentTransaction.commit();
        fm.popBackStack();

    }

    @OnClick(R.id.btnShare)
    public void onShare() {
        String title  = newsDict.optString("TITLE", "");
        String link = newsDict.optString("LINK", "");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, link);

        startActivity(Intent.createChooser(shareIntent, "Share"));
    }



    class myWebClient extends WebViewClient
    {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub
            progressBar.setVisibility(View.VISIBLE);
            view.loadUrl(url);
            return true;

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub
            super.onPageFinished(view, url);

            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }

        }
    }


}
