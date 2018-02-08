package mayah.zdalyapp.zdaly;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mayah.zdalyapp.zdaly.util.SetImageFromURL;

public class MarineDetailActivity extends AppCompatActivity {

    @BindView(R.id.txtName)
    TextView txtName;
    @BindView(R.id.txtLoc)
    TextView txtLoc;
    @BindView(R.id.txtDate)
    TextView txtDate;
    @BindView(R.id.imgWeather)
    ImageView imgWeather;
    @BindView(R.id.txtCondition)
    TextView txtCondition;
    @BindView(R.id.txtTemp)
    TextView txtTemp;
    @BindView(R.id.txtMaxTemp)
    TextView txtMaxTemp;
    @BindView(R.id.txtMinTemp)
    TextView txtMinTemp;
    @BindView(R.id.txtWindSpeed)
    TextView txtWindSpeed;
    @BindView(R.id.txtWindDirection)
    TextView txtWindDirection;
    @BindView(R.id.txtWindSpeedM)
    TextView txtWindSpeedM;
    @BindView(R.id.txtPrecipitation)
    TextView txtPrecipitation;
    @BindView(R.id.txtHumidity)
    TextView txtHumidity;
    @BindView(R.id.txtVisibility)
    TextView txtVisibility;
    @BindView(R.id.txtPressure)
    TextView txtPressure;

    @BindView(R.id.listView)
    ListView listView;

    public static JSONObject oceanDict;
    JSONArray marineArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marine_detail);
        ButterKnife.bind(this);

        marineArr = new JSONArray();
        setWeatherInfo();

    }

    @OnClick(R.id.btnClose)
    public void onClose() {
        finish();
    }

    private void setWeatherInfo() {

        try {
            txtName.setText(oceanDict.optString("name", ""));

            double latitude = oceanDict.optDouble("lat", 0.0f);
            double longitude = oceanDict.optDouble("lon", 0.0);
            txtLoc.setText(String.format("Latitude: %.2f, Longitude: %.2f", latitude, longitude));

            JSONObject marineDict = oceanDict.optJSONObject("weather");
            JSONObject dataDict = marineDict.optJSONObject("data");
            JSONArray weatherArr = dataDict.optJSONArray("weather");
            JSONObject weatherDict = weatherArr.getJSONObject(0);
            int maxTemp = weatherDict.optInt("maxtempC", 0);
            int minTemp = weatherDict.optInt("mintempC", 0);

            JSONArray hourlyArr = weatherDict.getJSONArray("hourly");
            JSONObject hourDict = hourlyArr.getJSONObject(0);
            int temp = hourDict.optInt("tempC", 0);
            int windspeed = hourDict.optInt("windspeedKmph", 0);
            int humidity = hourDict.optInt("humidity", 0);
            int visibility = hourDict.optInt("visibility", 0);

            txtMinTemp.setText(String.format("Low: %d\u2103", minTemp));
            txtMaxTemp.setText(String.format("High: %d\u2103", maxTemp));
            txtTemp.setText(String.format("%d\u2103   %s\u2109", temp, hourDict.optString("tempF", "")));
            txtWindSpeed.setText(String.format("Wind Speed: %dkm/h", windspeed));
            txtWindDirection.setText(hourDict.optString("winddir16Point", ""));
            txtWindSpeedM.setText(String.format("Wind Speed: %smiles/hour", hourDict.optString("windspeedMiles", "")));
            txtPrecipitation.setText("Precipitation: " + hourDict.optString("precipMM", "") + "mm");
            txtHumidity.setText(String.format("Humidity: %d%s", humidity, "%"));
            txtVisibility.setText(String.format("Visibility: %dkm", visibility));
            txtPressure.setText("Pressure: " + hourDict.optString("pressure", "") + "mb");

            JSONArray weatherIconUrlArr = hourDict.getJSONArray("weatherIconUrl");
            JSONObject weatherIconUrlDict = weatherIconUrlArr.getJSONObject(0);
            String weatherIconUrl = weatherIconUrlDict.optString("value", "");

            JSONArray weatherDescArr = hourDict.getJSONArray("weatherDesc");
            JSONObject weatherDescDict = weatherDescArr.getJSONObject(0);
            String weatherDesc = weatherDescDict.optString("value", "");

            new SetImageFromURL(imgWeather, weatherIconUrl).execute();
            txtCondition.setText(weatherDesc);
            txtDate.setText(weatherDict.optString("date", ""));

            marineArr = weatherArr;



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public class MarineDetailAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater inflater = null;

        public MarineDetailAdapter(Context context) {
            this.mContext = context;
            inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return marineArr.length();
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

            view = inflater.inflate(R.layout.row_marine_detail, null);
            holder = new ViewHolder(view);

            try {
                JSONObject weatherDict = marineArr.getJSONObject(position);
                int maxTemp = weatherDict.optInt("maxtempC", 0);
                int minTemp = weatherDict.optInt("mintempC", 0);

                JSONArray hourlyArr = weatherDict.getJSONArray("hourly");
                JSONObject hourDict = hourlyArr.getJSONObject(0);
                int temp = hourDict.optInt("tempC", 0);
                int windspeed = hourDict.optInt("windspeedKmph", 0);
                int humidity = hourDict.optInt("humidity", 0);
                int visibility = hourDict.optInt("visibility", 0);
                int pressure = hourDict.optInt("pressure", 0);

                holder.txtLow.setText(String.format("Low: %d\u2103", minTemp));
                holder.txtHigh.setText(String.format("High: %d\u2103", maxTemp));
                holder.txtWindSpeed.setText(String.format("Wind Speed: %dkm/h", windspeed));
                holder.txtWindDirection.setText(hourDict.optString("winddir16Point", ""));
                holder.txtWindSpeedM.setText(String.format("Wind Speed: %smiles/hour", hourDict.optString("windspeedMiles", "")));
                holder.txtPrecipitation.setText(String.format("Precipitation: %smm", hourDict.optString("precipMM", "")));
                holder.txtHumidity.setText(String.format("Humidity: %d%s", humidity, "%"));
                holder.txtVisibility.setText(String.format("Visibility: %dkm", visibility));
                holder.txtPressure.setText(String.format("Pressure: %dmb", pressure));

                JSONArray weatherIconUrlArr = hourDict.getJSONArray("weatherIconUrl");
                JSONObject weatherIconUrlDict = weatherIconUrlArr.getJSONObject(0);
                String weatherIconUrl = weatherIconUrlDict.optString("value", "");

                JSONArray weatherDescArr = hourDict.getJSONArray("weatherDesc");
                JSONObject weatherDescDict = weatherDescArr.getJSONObject(0);
                String weatherDesc = weatherDescDict.optString("value", "");

                new SetImageFromURL(holder.imgWeather, weatherIconUrl).execute();
                holder.txtCondition.setText(weatherDesc);
                holder.txtDate.setText(weatherDict.optString("date", ""));
                holder.txtTemp.setText(String.format("%d\u2103", temp));
                holder.txtTempF.setText(hourDict.optString("tempF", "") + "\u2109");

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return view;
        }
    }

    class ViewHolder {

        @BindView(R.id.imgWeather)
        ImageView imgWeather;
        @BindView(R.id.txtDate)
        TextView txtDate;
        @BindView(R.id.txtCondition)
        TextView txtCondition;
        @BindView(R.id.txtTemp)
        TextView txtTemp;
        @BindView(R.id.txtTempF)
        TextView txtTempF;
        @BindView(R.id.txtHigh)
        TextView txtHigh;
        @BindView(R.id.txtLow)
        TextView txtLow;
        @BindView(R.id.txtWindSpeed)
        TextView txtWindSpeed;
        @BindView(R.id.txtWindDirection)
        TextView txtWindDirection;
        @BindView(R.id.txtWindSpeedM)
        TextView txtWindSpeedM;
        @BindView(R.id.txtPrecipitation)
        TextView txtPrecipitation;
        @BindView(R.id.txtHumidity)
        TextView txtHumidity;
        @BindView(R.id.txtVisibility)
        TextView txtVisibility;
        @BindView(R.id.txtPressure)
        TextView txtPressure;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
