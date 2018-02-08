package mayah.zdalyapp.zdaly.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mayah.zdalyapp.zdaly.MainActivity;
import mayah.zdalyapp.zdaly.MarineDetailActivity;
import mayah.zdalyapp.zdaly.R;
import mayah.zdalyapp.zdaly.WeatherDetailActivity;
import mayah.zdalyapp.zdaly.util.Constant;
import mayah.zdalyapp.zdaly.util.SetImageFromURL;
import mayah.zdalyapp.zdaly.util.Util;

public class WeatherForecastFragment extends Fragment {

    @BindView(R.id.btnCity)
    Button btnCity;
    @BindView(R.id.cityUnderline)
    View cityUnderline;
    @BindView(R.id.btnOcean)
    Button btnOcean;
    @BindView(R.id.oceanUnderline)
    View oceanUnderline;
    @BindView(R.id.listView)
    ListView listView;
    @BindView(R.id.mapView)
    View mapView;

    String userid;
    JSONArray weatherArr;
    JSONArray marineArr;
    int selectedOceanIndex;
    JSONArray annotArr;
    int tabIndex = 0;
    int oceanViewType;

    WeatherForecastAdapter listAdapter;

    public WeatherForecastFragment() {
        // Required empty public constructor
        weatherArr = new JSONArray();
        marineArr = new JSONArray();
        selectedOceanIndex = 0;
        tabIndex = 0;
        oceanViewType = 0;
    }

    public static WeatherForecastFragment newInstance() {
        WeatherForecastFragment fragment = new WeatherForecastFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather_forecast, container, false);
        ButterKnife.bind(this, view);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constant.SHARED_PR.SHARE_PREF, Context.MODE_PRIVATE);
        userid = sharedPreferences.getString(Constant.SHARED_PR.KEY_ID, "");

        ((MainActivity)getActivity()).showLoadingDialog("Getting weather list..");
        (new GetWeatherForecast()).execute();

        return view;
    }

    @OnClick(R.id.btnCity)
    public void onCity() {
        tabIndex = 0;
        btnOcean.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        oceanUnderline.setVisibility(View.GONE);
        btnCity.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
        cityUnderline.setVisibility(View.VISIBLE);


        listView.setVisibility(View.VISIBLE);
        mapView.setVisibility(View.GONE);
        listAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.btnOcean)
    public void onOcean() {
        tabIndex = 1;
        btnCity.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        cityUnderline.setVisibility(View.GONE);
        btnOcean.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
        oceanUnderline.setVisibility(View.VISIBLE);

        listView.setVisibility(View.VISIBLE);
        mapView.setVisibility(View.GONE);
        listAdapter.notifyDataSetChanged();

    }


    class GetWeatherForecast extends AsyncTask<Void, String, String> {

        String response;

        public GetWeatherForecast() {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                response = Util.getRequest(Constant.WEATHER_FORECAST_URL + "?id=" + userid);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ((MainActivity)getActivity()).hideLoadingDialog();

            try {
                JSONObject resultDic = new JSONObject(result);
                weatherArr = resultDic.getJSONArray("weather");
                marineArr = resultDic.getJSONArray("marine");

//                updateMap();

                listAdapter = new WeatherForecastAdapter(getActivity());
                listView.setAdapter(listAdapter);

            } catch (JSONException e) {
                toast("Sorry! No data found.");
            }
        }
    }


    class WeatherForecastAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater inflater = null;

        public WeatherForecastAdapter(Context context) {
            this.mContext = context;
            inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            int len = 0;
            if (tabIndex == 0) {
                len = weatherArr.length();
            } else {
                len = marineArr.length();
            }
            return len;
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

            if (tabIndex == 0) {
                view = inflater.inflate(R.layout.row_weather_forcast, null);
                WeatherViewHolder holder = new WeatherViewHolder(view);

                try {
                    final JSONObject weatherDict = weatherArr.getJSONObject(position);
                    holder.setWeatherDict(weatherDict);

                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            WeatherDetailActivity.weatherDict = weatherDict;
                            Intent intent = new Intent(getActivity(), WeatherDetailActivity.class);
                            startActivity(intent);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                view = inflater.inflate(R.layout.row_ocean, null);
                OceanViewHolder holder = new OceanViewHolder(view);

                try {
                    final JSONObject marineDict = marineArr.getJSONObject(position);
                    holder.setOceanDict(marineDict);

                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MarineDetailActivity.oceanDict = marineDict;
                            Intent intent = new Intent(getActivity(), MarineDetailActivity.class);
                            startActivity(intent);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            return view;
        }
    }

    class WeatherViewHolder {

        @BindView(R.id.txtName)
        TextView txtName;
        @BindView(R.id.txtTempC)
        TextView txtTempC;
        @BindView(R.id.txtTempF)
        TextView txtTempF;
        @BindView(R.id.txtWindSpeed)
        TextView txtWindSpeed;
        @BindView(R.id.txtHumidity)
        TextView txtHumidity;
        @BindView(R.id.txtDesc)
        TextView txtDesc;
        @BindView(R.id.imgWeather)
        ImageView imgWeather;

        public WeatherViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        public void setWeatherDict(JSONObject object) {
            try {
                JSONObject dataDict = object.optJSONObject("data");
                JSONArray requestArr = dataDict.optJSONArray("request");
                JSONObject requestDict = requestArr.getJSONObject(0);

                txtName.setText(requestDict.optString("query", ""));
                JSONArray conditionArr = dataDict.optJSONArray("current_condition");
                JSONObject conditionDict = conditionArr.getJSONObject(0);

                txtTempC.setText(conditionDict.optString("temp_C", "") + " \u2103");
                txtTempF.setText(conditionDict.optString("temp_F", "") + " \u2109");
                txtWindSpeed.setText(conditionDict.optString("windspeedKmph", "") + "km/h");
                txtHumidity.setText(conditionDict.optString("humidity", "") + "%");

                JSONArray weatherIconUrlArr = conditionDict.getJSONArray("weatherIconUrl");
                JSONObject weatherIconUrlDict = weatherIconUrlArr.getJSONObject(0);
                String weatherIconUrl = weatherIconUrlDict.optString("value", "");
                JSONArray weatherDescArr = conditionDict.getJSONArray("weatherDesc");
                JSONObject weatherDescDict = weatherDescArr.getJSONObject(0);
                String weatherDesc = weatherDescDict.optString("value", "");

                (new SetImageFromURL(imgWeather, weatherIconUrl)).execute();

                txtDesc.setText(weatherDesc);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    class OceanViewHolder {
        @BindView(R.id.txtName)
        TextView txtName;
        @BindView(R.id.txtLatitude)
        TextView txtLatitude;
        @BindView(R.id.txtLongitude)
        TextView txtLongitude;

        public OceanViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        public void setOceanDict(JSONObject oceanDict) {
            txtName.setText(oceanDict.optString("name", ""));
            float latitude = (float)oceanDict.optDouble("lat", 0.0);
            float longitude = (float)oceanDict.optDouble("lon", 0.0);
            txtLatitude.setText(String.format("%.2f", latitude));
            txtLongitude.setText(String.format("%.2f", longitude));
        }
    }

    private void toast(CharSequence text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }
}
