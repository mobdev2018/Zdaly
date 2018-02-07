package mayah.zdalyapp.zdaly;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mayah.zdalyapp.zdaly.fragments.DailyNewsFragment;
import mayah.zdalyapp.zdaly.fragments.DailyNewsWebViewFragment;
import mayah.zdalyapp.zdaly.fragments.SpotPricesFragment;
import mayah.zdalyapp.zdaly.fragments.WeatherForecastFragment;

public class MainActivity extends FragmentActivity {

    @BindView(R.id.btnDailyNews)
    Button btnDailyNews;
    @BindView(R.id.btnKeyTrends)
    Button btnKeyTrends;
    @BindView(R.id.btnSpotPrices)
    Button btnSpotPrices;
    @BindView(R.id.btnWeatherForecast)
    Button btnWeatherForecast;
    @BindView(R.id.frame_container)
    FrameLayout frameLayout;

    @BindView(R.id.loadingView)
    RelativeLayout loadingView;
    @BindView(R.id.txtLoading)
    TextView txtLoading;

    private int selectedIndex = 1;

    Fragment fragment;
    FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        selectedIndex = 1;
        displayView(selectedIndex);
    }

    private void displayView(int index) {
        selectedIndex = index;
        switch (selectedIndex) {
            case 1:
                fragment = DailyNewsFragment.newInstance();
                break;
            case 2:
                break;
            case 3:
                fragment = SpotPricesFragment.newInstance();
                break;
            case 4:
                fragment = WeatherForecastFragment.newInstance();
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();

            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_container, fragment).commit();

        }
    }

    public void showLoadingDialog(String text) {
        txtLoading.setText(text);
        loadingView.setAlpha(1);
        loadingView.setVisibility(View.VISIBLE);
    }

    public void hideLoadingDialog() {
        loadingView.animate()
                .alpha(0.0f)
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        super.onAnimationCancel(animation);
                        loadingView.setVisibility(View.GONE);
                    }
                });
    }

    public void showDailyNewsWebView(JSONObject object) {
        DailyNewsWebViewFragment dailyNewsWebViewFragment = DailyNewsWebViewFragment.newInstance(object);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.frame_container, dailyNewsWebViewFragment);
        ft.commit();
    }

    @OnClick(R.id.btnDailyNews)
    public void onDailyNews(View view) {
        btnDailyNews.setBackgroundResource(R.color.red);
        btnKeyTrends.setBackgroundResource(R.color.colorPrimaryDark);
        btnSpotPrices.setBackgroundResource(R.color.colorPrimaryDark);
        btnWeatherForecast.setBackgroundResource(R.color.colorPrimaryDark);
        displayView(1);
    }

    @OnClick(R.id.btnKeyTrends)
    public void onKeyTrends(View view) {
        btnDailyNews.setBackgroundResource(R.color.colorPrimaryDark);
        btnKeyTrends.setBackgroundResource(R.color.red);
        btnSpotPrices.setBackgroundResource(R.color.colorPrimaryDark);
        btnWeatherForecast.setBackgroundResource(R.color.colorPrimaryDark);
        displayView(2);
    }

    @OnClick(R.id.btnSpotPrices)
    public void onSpotPrices(View view) {
        btnDailyNews.setBackgroundResource(R.color.colorPrimaryDark);
        btnKeyTrends.setBackgroundResource(R.color.colorPrimaryDark);
        btnSpotPrices.setBackgroundResource(R.color.red);
        btnWeatherForecast.setBackgroundResource(R.color.colorPrimaryDark);
        displayView(3);
    }

    @OnClick(R.id.btnWeatherForecast)
    public void onWeatherForecast(View view) {
        btnDailyNews.setBackgroundResource(R.color.colorPrimaryDark);
        btnKeyTrends.setBackgroundResource(R.color.colorPrimaryDark);
        btnSpotPrices.setBackgroundResource(R.color.colorPrimaryDark);
        btnWeatherForecast.setBackgroundResource(R.color.red);
        displayView(4);
    }

}
