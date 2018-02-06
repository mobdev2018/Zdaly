package mayah.zdalyapp.zdaly.fragments;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import mayah.zdalyapp.zdaly.R;

public class DailyNewsWebViewFragment extends Fragment {

    @BindView(R.id.webView)
    WebView webView;

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
        ButterKnife.bind(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        String websiteUrl = newsDict.optString("LINK");
        webView.loadUrl(websiteUrl);
    }


}
