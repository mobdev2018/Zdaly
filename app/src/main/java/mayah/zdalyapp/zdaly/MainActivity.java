package mayah.zdalyapp.zdaly;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.app.FragmentActivity;
import android.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mayah.zdalyapp.zdaly.fragments.DailyNewsFragment;

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
                break;
            case 4:
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
