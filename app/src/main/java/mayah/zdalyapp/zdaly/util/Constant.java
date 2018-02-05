package mayah.zdalyapp.zdaly.util;

import okhttp3.MediaType;

/**
 * Created by Hello on 2/4/2018.
 */

public class Constant {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static String URL = "https://zdaly.com";
    public static String AUTHENTICATE_URL = "https://zdaly.com/account/autorize";
    public static String DAILY_NEWS_URL = "https://zdaly.com/new/newlist";
    public static String KEY_TRENDS_URL = "https://zdaly.com/new/chartlist";
    public static String SPOT_PRICES_URL = "https://zdaly.com/new/stocklist";
    public static String WEATHER_FORECAST_URL = "https://zdaly.com/new/weathermarinelist";

    public static String MARINE_FORECAST_URL = "http://api.worldweatheronline.com/premium/v1/marine.ashx";

    public static String network_error = "Please check your network connectivity.";
    public static String AppName = "Zdaly";

    public static class SHARED_PR {
        public static final String SHARE_PREF = AppName + "_preferences";
        public static final String KEY_EMAIL = "email";
        public static final String KEY_PASSWORD = "password";
        public static final String KEY_ID = "id";
    }
}
