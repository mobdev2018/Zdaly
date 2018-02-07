package mayah.zdalyapp.zdaly.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import mayah.zdalyapp.zdaly.MainActivity;
import mayah.zdalyapp.zdaly.R;
import mayah.zdalyapp.zdaly.util.Constant;
import mayah.zdalyapp.zdaly.util.Util;


public class SpotPricesFragment extends Fragment {

    @BindView(R.id.listview)
    ListView listView;

    String userid;
    JSONArray spotPricesArr;
    SpotPricesListAdapter listAdapter;

    public SpotPricesFragment() {
        // Required empty public constructor
    }

    public static SpotPricesFragment newInstance() {
        SpotPricesFragment fragment = new SpotPricesFragment();

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_spot_prices, container, false);
        ButterKnife.bind(this, view);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constant.SHARED_PR.SHARE_PREF, Context.MODE_PRIVATE);
        userid = sharedPreferences.getString(Constant.SHARED_PR.KEY_ID, "");

        spotPricesArr = new JSONArray();
        ((MainActivity)getActivity()).showLoadingDialog("Getting spot prices..");

        (new getSpotPrices()).execute();
        return view;
    }

    class getSpotPrices extends AsyncTask<Void, String, String> {

        String response;

        public getSpotPrices() {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                response = Util.getRequest(Constant.SPOT_PRICES_URL + "?id=" + userid);
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
                JSONArray commodityArr = resultDic.optJSONArray("commodity");
                JSONArray stockArr = resultDic.optJSONArray("stock");
                JSONArray currencyArr = resultDic.optJSONArray("currency");

                for (int i = 0; i < commodityArr.length(); i++) {
                    JSONObject obj = commodityArr.getJSONObject(i);
                    obj.put("type", 0);
                    spotPricesArr.put(obj);
                }

                for (int i = 0; i < stockArr.length(); i++) {
                    JSONObject obj = stockArr.getJSONObject(i);
                    obj.put("type", 0);
                    spotPricesArr.put(obj);
                }

                for (int i = 0; i < currencyArr.length(); i++) {
                    JSONObject obj = currencyArr.getJSONObject(i);
                    obj.put("type", 1);
                    spotPricesArr.put(obj);
                }

                listAdapter = new SpotPricesListAdapter(getActivity());
                listView.setAdapter(listAdapter);

            } catch (JSONException e) {
                toast("Sorry! No data found.");
            }
        }
    }

    public class SpotPricesListAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater inflater = null;

        public SpotPricesListAdapter(Context context) {
            this.mContext = context;
            inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return spotPricesArr.length();
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

            try {
                JSONObject spotPriceDict = spotPricesArr.getJSONObject(position);

                int type = spotPriceDict.optInt("type");

                switch (type) {
                    case 0:
                    {
                        view = inflater.inflate(R.layout.row_stock_commodity, null);
                        StockCommodityViewHolder holder = new StockCommodityViewHolder(view);

                        holder.txtName.setText(spotPriceDict.optString("Name", ""));
                        float lastTradePriceOnly = (float) spotPriceDict.optDouble("LastTradePriceOnly", 0.0f);
                        holder.txtOrigin.setText(String.format("%.2f", lastTradePriceOnly));
                        holder.txtChange.setText(spotPriceDict.optString("Change", ""));

                        break;
                    }
                    case 1:
                    {
                        view = inflater.inflate(R.layout.row_spot_prices, null);
                        SpotPricesViewHolder holder = new SpotPricesViewHolder(view);

                        holder.txtName.setText(spotPriceDict.optString("Name", ""));
                        holder.txtRate.setText(spotPriceDict.optString("Rate", ""));

                        break;
                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return view;
        }
    }

    class SpotPricesViewHolder {

        @BindView(R.id.txtName)
        TextView txtName;
        @BindView(R.id.txtRate)
        TextView txtRate;

        public SpotPricesViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

    }

    class StockCommodityViewHolder {

        @BindView(R.id.txtName)
        TextView txtName;
        @BindView(R.id.txtOrigin)
        TextView txtOrigin;
        @BindView(R.id.txtChange)
        TextView txtChange;

        public StockCommodityViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

    }

    private void toast(CharSequence text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

}
