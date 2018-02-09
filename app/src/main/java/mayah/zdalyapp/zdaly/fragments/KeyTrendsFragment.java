package mayah.zdalyapp.zdaly.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.zip.Inflater;

import butterknife.BindView;
import butterknife.ButterKnife;
import mayah.zdalyapp.zdaly.MainActivity;
import mayah.zdalyapp.zdaly.R;
import mayah.zdalyapp.zdaly.util.Constant;
import mayah.zdalyapp.zdaly.util.Util;

public class KeyTrendsFragment extends Fragment {

    @BindView(R.id.headerView)
    LinearLayout headerView;
    @BindView(R.id.listView)
    ListView listView;

    String userid;
    int tabIndex;
    JSONArray industryArr;
    JSONArray pricesArr;
    JSONArray economyArr;

    ArrayList<String> headerTitleArr;
    ArrayList<View> headerButtonsArr;
    JSONObject headerWithDataDict;

    public static ArrayList<String> graphColorArr = new ArrayList<String>();


    public KeyTrendsFragment() {
        // Required empty public constructor
    }

    public static KeyTrendsFragment newInstance() {
        KeyTrendsFragment fragment = new KeyTrendsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_key_trends, container, false);
        ButterKnife.bind(this, view);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constant.SHARED_PR.SHARE_PREF, Context.MODE_PRIVATE);
        userid = sharedPreferences.getString(Constant.SHARED_PR.KEY_ID, "");

        tabIndex = 0;
        getEachTabGraphInfo();

        return view;
    }

    private void getEachTabGraphInfo() {
        headerTitleArr = new ArrayList<String>();
        headerButtonsArr = new ArrayList<View>();
        headerWithDataDict = new JSONObject();

        industryArr = new JSONArray();
        pricesArr = new JSONArray();
        economyArr = new JSONArray();

        ((MainActivity)getActivity()).showLoadingDialog("Getting trends..");
        new GetKeyTrends().execute();
    }


    class GetKeyTrends extends AsyncTask<Void, String, String> {
        String response;

        public GetKeyTrends() {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                response = Util.getRequest(Constant.KEY_TRENDS_URL + "?id=" + userid);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            int maxColorCnt = 0;
            ((MainActivity)getActivity()).hideLoadingDialog();

            try {
                JSONArray graphArr = new JSONArray(result);
                if (graphArr==null) return;

                for (int i = 0; i < graphArr.length(); i++) {
                    JSONObject graphDict = graphArr.getJSONObject(i);
                    JSONArray configurationArr = graphDict.getJSONArray("configuration");
                    if (configurationArr.length() > maxColorCnt) {
                        maxColorCnt = configurationArr.length();
                    }

                    String type = graphDict.optString("type", "");
                    if (!type.equals("")) {
                        if (type.equals("Industry")) {
                            industryArr.put(graphDict);
                        } else if (type.equals("Prices")) {
                            pricesArr.put(graphDict);
                        } else {
                            economyArr.put(graphDict);
                        }
                    }

                    if (!type.equals("")) {
                        if (!headerTitleArr.contains(type)) {
                            headerTitleArr.add(type);

                            JSONArray tempArr = new JSONArray();
                            for (int j = 0; j < graphArr.length(); j++) {
                                JSONObject tempDict = graphArr.getJSONObject(j);
                                if (type.equals(tempDict.optString("type", ""))) {
                                    tempArr.put(tempDict);
                                }
                            }
                            if (tempArr.length() > 0) {
                                headerWithDataDict.put(type, tempArr);
                            }
                        }
                    }
                }

                createDynamicScrollView();

                if (maxColorCnt > graphColorArr.size()) {
                    addColorsToGraphColorArr(maxColorCnt - graphColorArr.size());
                }

//                listView.setAdapter();

            } catch (JSONException e) {
                toast("Sorry! No data found.");
            }
        }
    }

    private void createDynamicScrollView() {

        for (int i = 0; i < headerTitleArr.size(); i++) {

            final int index = i;
            String title = headerTitleArr.get(i);

            View barButton = LayoutInflater.from(getContext()).inflate(R.layout.view_button, headerView, false);

            Button button = (Button)barButton.findViewById(R.id.btnTitle);
            View underline = (View) barButton.findViewById(R.id.underline);
            button.setText(title);
            button.setTag(0);
            underline.setTag(1);
            if (i == 0) {
                button.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                underline.setVisibility(View.VISIBLE);
            } else {
                button.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
                underline.setVisibility(View.GONE);
            }

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int j = 0; j < headerButtonsArr.size(); j++) {
                        View buttonView = headerButtonsArr.get(j);
                        Button btn = (Button) buttonView.findViewWithTag(0);
                        View underline = (View) buttonView.findViewWithTag(1);

                        if (index == j) {
                            btn.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                            underline.setVisibility(View.VISIBLE);
                            setTabIndex(index);
                        } else {
                            btn.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
                            underline.setVisibility(View.GONE);
                        }

                    }
                }
            });

            headerButtonsArr.add(barButton);
            headerView.addView(barButton);
        }
    }

    public void setTabIndex(int index) {
        tabIndex = index;
    }





    public void addColorsToGraphColorArr(int count) {
        for (int i = 0; i < count; i++) {
            int r = (int)(Math.random() * 100) % 255;
            int g = (int)(Math.random() * 100) % 255;
            int b = (int)(Math.random() * 100) % 255;

            int newColor = Color.rgb(r, g, b);

            graphColorArr.add(String.valueOf(newColor));
        }
    }


    private void toast(CharSequence text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    private boolean containsObject(JSONArray arr, JSONObject obj) {
        try {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject tmp = arr.getJSONObject(i);
                if (tmp.equals(obj)) {
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

}
