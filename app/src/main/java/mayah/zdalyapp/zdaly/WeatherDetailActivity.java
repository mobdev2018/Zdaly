package mayah.zdalyapp.zdaly;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mayah.zdalyapp.zdaly.fragments.DailyNewsFragment;
import mayah.zdalyapp.zdaly.util.SetImageFromURL;

public class WeatherDetailActivity extends AppCompatActivity {

    @BindView(R.id.txtName)
    TextView txtName;
    @BindView(R.id.txtDate)
    TextView txtDate;
    @BindView(R.id.imgWeather)
    ImageView imgWeather;
    @BindView(R.id.txtCondition)
    TextView txtCondition;
    @BindView(R.id.txtTemp)
    TextView txtTemp;
    @BindView(R.id.txtWindSpeed)
    TextView txtWindSpeed;
    @BindView(R.id.txtPressure)
    TextView txtPressure;
    @BindView(R.id.txtHumidity)
    TextView txtHumidity;
    @BindView(R.id.txtVisibility)
    TextView txtVisibility;
    @BindView(R.id.listView)
    ListView listView;

    public static JSONObject weatherDict;
    private JSONArray weatherArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_detail);
        ButterKnife.bind(this);

        showData();
    }

    private void showData() {
        try {
            JSONObject dataDict = weatherDict.optJSONObject("data");

            JSONArray requestArr = dataDict.optJSONArray("request");
            JSONObject requestDict = requestArr.getJSONObject(0);
            txtName.setText(requestDict.optString("query", ""));
            JSONArray conditionArr = dataDict.optJSONArray("current_condition");
            JSONObject conditionDict = conditionArr.getJSONObject(0);
            txtTemp.setText(conditionDict.optString("temp_C", "") + "\u2103");
            txtWindSpeed.setText("Wind Speed: " + conditionDict.optString("windspeedKmph", "") + "km/h");
            txtPressure.setText("Pressure: " + conditionDict.optString("pressure", "") + "mb");
            txtHumidity.setText("Humidity: " + conditionDict.optString("humidity", "") + "%");
            txtVisibility.setText("Visibility: " + conditionDict.optString("visisbility", "") + "km");

            JSONArray weatherIconUrlArr = conditionDict.optJSONArray("weatherIconUrl");
            JSONObject weatherIconUrlDict = weatherIconUrlArr.getJSONObject(0);
            String weatherIconUrl = weatherIconUrlDict.optString("value", "");
            (new SetImageFromURL(imgWeather, weatherIconUrl)).execute();

            JSONArray weatherDescArr = conditionDict.optJSONArray("weatherDesc");
            JSONObject weatherDescDict = weatherDescArr.getJSONObject(0);
            txtCondition.setText(weatherDescDict.optString("value", ""));

            weatherArr = dataDict.optJSONArray("weather");
            JSONObject weatherDict = weatherArr.getJSONObject(0);
            txtDate.setText(weatherDict.optString("date", ""));

            WeatherDetailAdapter adapter = new WeatherDetailAdapter(getApplicationContext());
            listView.setAdapter(adapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @OnClick(R.id.btnClose)
    public void onClose() {
        weatherDict = null;
        finish();
    }

    public class WeatherDetailAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater inflater = null;

        public WeatherDetailAdapter(Context context) {
            this.mContext = context;
            inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return weatherArr.length();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {

            final ViewHolder holder;

            view = inflater.inflate(R.layout.row_weekday_weather, null);
            holder = new ViewHolder(view);

            try {
                JSONObject dataDict = weatherDict.optJSONObject("data");
                weatherArr = dataDict.optJSONArray("weather");
                JSONObject weekWeatherDict = weatherArr.getJSONObject(position);

                holder.txtDate.setText(weekWeatherDict.optString("date", ""));
                holder.txtHigh.setText("High: " + weekWeatherDict.optString("maxtempC", "") + "\u2103");
                holder.txtLow.setText("Low: " + weekWeatherDict.optString("mintempC", "") + "\u2103");

                JSONArray hourlyArr = weekWeatherDict.optJSONArray("hourly");
                JSONObject hourDict = hourlyArr.getJSONObject(0);
                JSONArray weatherIconUrlArr = hourDict.optJSONArray("weatherIconUrl");
                JSONObject weatherIconUrlDict = weatherIconUrlArr.getJSONObject(0);

                String weatherIconUrl = weatherIconUrlDict.optString("value", "");

                JSONArray weatherDescArr = hourDict.optJSONArray("weatherDesc");
                JSONObject weatherDescDict = weatherDescArr.getJSONObject(0);
                String weatherDesc = weatherDescDict.optString("value", "");

                new SetImageFromURL(holder.imgWeather, weatherIconUrl).execute();
                holder.txtCondition.setText(weatherDesc);

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return view;
        }
    }

    class ViewHolder {

        @BindView(R.id.txtDate)
        TextView txtDate;
        @BindView(R.id.txtCondition)
        TextView txtCondition;
        @BindView(R.id.txtHigh)
        TextView txtHigh;
        @BindView(R.id.txtLow)
        TextView txtLow;
        @BindView(R.id.imgWeather)
        ImageView imgWeather;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}
