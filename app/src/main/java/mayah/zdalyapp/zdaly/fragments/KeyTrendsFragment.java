package mayah.zdalyapp.zdaly.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.zip.Inflater;

import butterknife.BindView;
import butterknife.ButterKnife;
import mayah.zdalyapp.zdaly.MainActivity;
import mayah.zdalyapp.zdaly.MarineDetailActivity;
import mayah.zdalyapp.zdaly.R;
import mayah.zdalyapp.zdaly.WeatherDetailActivity;
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

    KeyTrendsListAdapter listAdapter;

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
                if (graphArr == null) return;

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

        setTabIndex(0);
    }

    public void setTabIndex(int index) {
        tabIndex = index;
        listAdapter = new KeyTrendsListAdapter(getContext());
        listView.setAdapter(listAdapter);
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

    class KeyTrendsListAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater inflater = null;
        JSONArray graphArr;

        public KeyTrendsListAdapter(Context context) {
            this.mContext = context;
            inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            graphArr = new JSONArray();
            try {
                graphArr = headerWithDataDict.getJSONArray(headerTitleArr.get(tabIndex));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getCount() {
            return graphArr.length();
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

            view = inflater.inflate(R.layout.row_key_trends, null);
            ViewHolder holder = new ViewHolder(view);

            try {
                JSONObject graphDict = graphArr.getJSONObject(position);
                holder.setGraphDict(graphDict);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return view;
        }
    }

    class ViewHolder {

        @BindView(R.id.txtTitle)
        TextView txtTitle;
        @BindView(R.id.descView)
        LinearLayout descView;


        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        public void setGraphDict(JSONObject graphDict) {

            int barWidth = 20;
            int barSpace = 4;
            int barGroupSpace = 16;
            int startOffset = 64;
            int yAxisHeight = 138-2;
            int columnCnt = 0;

            try {
                boolean isStack = graphDict.optBoolean("isStack", false);

                txtTitle.setText(graphDict.optString("title", ""));

                JSONArray configurationArr = graphDict.getJSONArray("configuration");

                float minYAxis;
                float maxYAxis;

                ArrayList yAxisStrArr = new ArrayList();

                //========================================================
                //=========   Configure Description Section    ===========
                //========================================================

                //--------- Calculate max width of bar description label and column count --------

                for (int i = 0; i < configurationArr.length(); i++) {
                    JSONObject configurationDict = configurationArr.getJSONObject(i);
                    String title = configurationDict.optString("title", "");


                    String type = configurationDict.optString("type", "");
                    if (type.equals("column")) {
                        columnCnt++;
                    }
                }

                if (isStack) columnCnt = 1;

                for (int i = 0; i < configurationArr.length(); i++) {
                    JSONObject configurationDict = configurationArr.getJSONObject(i);
                    String title = configurationDict.optString("title", "");
                    String type = configurationDict.optString("type", "");

                    int fillColor;
                    String fillColorString = configurationDict.optString("fillColor", null);

                    if (fillColorString == null) {
                        fillColor = Integer.parseInt(graphColorArr.get(i));
                    } else {
                        Log.e("========", fillColorString);
                        fillColor = Color.parseColor(fillColorString);
                    }

                    int lineColor;
                    String lineColorString = configurationDict.optString("lineColor", null);
                    if (lineColorString == null) {
                        lineColor = Integer.parseInt(graphColorArr.get(i));
                    } else {
                        lineColor = Color.parseColor(lineColorString);
                    }

                    LinearLayout barDescGroup;
                    int tag = i / 2;
                    if ((i % 2) == 0) {
                        barDescGroup = new LinearLayout(getContext());
                        barDescGroup.setOrientation(LinearLayout.VERTICAL);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParams.rightMargin = 24;
                        barDescGroup.setLayoutParams(layoutParams);
                        barDescGroup.setTag(tag);
                        descView.addView(barDescGroup);

                    } else {
                        barDescGroup = (LinearLayout)descView.findViewWithTag(tag);
                    }

                    LinearLayout.LayoutParams barDescViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    barDescViewParams.setMargins(0, 2, 0, 2);
                    LinearLayout barDescView = new LinearLayout(getContext());
                    barDescView.setOrientation(LinearLayout.HORIZONTAL);
                    barDescView.setLayoutParams(barDescViewParams);
                    barDescView.setGravity(Gravity.CENTER_VERTICAL);

                    if (type.equals("column")) {
                        LinearLayout.LayoutParams columnParams = new LinearLayout.LayoutParams(100, 40);
                        columnParams.rightMargin = 8;
                        View columnView = new View(getContext());
                        columnView.setLayoutParams(columnParams);
                        columnView.setBackgroundColor(fillColor);

                        barDescView.addView(columnView);

                    } else {
                        RelativeLayout.LayoutParams lineViewParams = new RelativeLayout.LayoutParams(100, 40);
                        RelativeLayout lineView = new RelativeLayout(getContext());
                        lineView.setLayoutParams(lineViewParams);

                        RelativeLayout.LayoutParams lineParams = new RelativeLayout.LayoutParams(100, 3);
                        lineParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                        LinearLayout line = new LinearLayout(getContext());
                        line.setLayoutParams(lineParams);
                        line.setBackgroundColor(lineColor);
                        lineView.addView(line);

                        RelativeLayout.LayoutParams circleParam = new RelativeLayout.LayoutParams(20, 20);
                        circleParam.addRule(RelativeLayout.CENTER_IN_PARENT);
                        TextView dot = new TextView(getContext());
                        dot.setLayoutParams(circleParam);
                        dot.setBackgroundColor(lineColor);
                        dot.setBackground(Util.drawCircle(getContext(), 20, 20, lineColor));
                        lineView.addView(dot);
                        barDescView.addView(lineView);
                    }

                    LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    TextView txtDesc = new TextView(getContext());
                    txtDesc.setLayoutParams(descParams);
                    txtDesc.setTextColor(ContextCompat.getColor(getContext(), R.color.darkGray));
                    txtDesc.setTextSize(16);
                    txtDesc.setText(title);

                    barDescView.addView(txtDesc);
                    barDescGroup.addView(barDescView);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
