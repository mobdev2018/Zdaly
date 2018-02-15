package mayah.zdalyapp.zdaly.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mayah.zdalyapp.zdaly.GraphShowActivity;
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

            Random random = new Random();
            int r = random.nextInt(200) + 55;
            int g = random.nextInt(200) + 55;
            int b = random.nextInt(200) + 55;

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
            final ViewHolder holder = new ViewHolder(view);

            try {
                final JSONObject graphDict = graphArr.getJSONObject(position);

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
        @BindView(R.id.graphView)
        View graphView;

        @BindView(R.id.yAxisView)
        ImageView yAxisView;
        @BindView(R.id.xBaseView)
        ImageView xBaseView;
        @BindView(R.id.imgGraph)
        ImageView imgGraph;

        JSONObject graphDict;
        JSONArray configurationArr;
        int columnCnt = 0;
        boolean isStack = false;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);

            graphDict = new JSONObject();
            configurationArr = new JSONArray();
        }


        @OnClick(R.id.btnZoom)
        public void onZoom() {
            GraphShowActivity.graphDict = this.graphDict;
            Intent intent = new Intent(getActivity(), GraphShowActivity.class);
            startActivity(intent);
        }

        public void setGraphDict(JSONObject graphDict)  {
            this.graphDict = graphDict;
            showHeaderView();
            showGraph();
        }

        public void showHeaderView() {

            try {
                isStack = graphDict.optBoolean("isStack", false);

                txtTitle.setText(graphDict.optString("title", ""));

                configurationArr = graphDict.getJSONArray("configuration");

                //========================================================
                //=========   Configure Description Section    ===========
                //========================================================

                //--------- Calculate max width of bar description label and column count --------
                for (int i = 0; i < configurationArr.length(); i++) {
                    JSONObject configurationDict = configurationArr.getJSONObject(i);
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
                    String fillColorString = configurationDict.optString("fillColors", null);

                    if (fillColorString == null) {
                        fillColor = Integer.parseInt(graphColorArr.get(i));
                    } else {
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
                        layoutParams.setMargins(0, 0, 30, 0);
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
                    txtDesc.setTextSize(14);
                    txtDesc.setText(title);

                    barDescView.addView(txtDesc);
                    barDescGroup.addView(barDescView);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void showGraph(){

            int barWidth = 20;
            int barSpace = 4;
            int barGroupSpace = 16;
            int startOffset = 16;
            int yAxisHeight = 138-2;

            float minYAxis;
            float maxYAxis;

            ArrayList<String> yAxisStrArr = new ArrayList();

            try {
                //========================================================
                //==================   Draw Bar Graph    =================
                //========================================================

                float barGroupWidth = columnCnt * (barWidth + barSpace);
                if (columnCnt == 0) {
                    barGroupSpace = barGroupSpace + barWidth;
                }

                //--------------- Configure Chart Bar Value Array ----------------//
                JSONArray values = graphDict.getJSONArray("values");

                JSONObject maxValDict = values.getJSONObject(0);
                for (int i = 1; i < values.length(); i++) {
                    JSONObject value = values.getJSONObject(i);
                    double maxTotal = maxValDict.optDouble("Total", 0);
                    double total = value.optDouble("Total", 0);
                    if (maxTotal < total) {
                        maxValDict = value;
                    }
                }

                float eachValHeight;
                float maxBarVal = 0;
                float minBarVal = 1000000;

                if (isStack) {
                    for (int i = 0; i < values.length(); i++) {
                        JSONObject graphGroupValueDict = values.getJSONObject(i);
                        float sumVal = 0;

                        for (int j = 0; j < configurationArr.length(); j++) {
                            JSONObject configurationDict = configurationArr.getJSONObject(j);
                            String title = configurationDict.optString("title", "");

                            float barVal = (float)graphGroupValueDict.optDouble(title, 0.0);
                            sumVal += barVal;
                        }

                        if (sumVal > maxBarVal) {
                            maxBarVal = sumVal;
                        }
                    }

                    maxYAxis = maxBarVal + maxBarVal / 50;
                    minYAxis = 0;

                    // New Implementation added below 15-12-2017

                    float maxBarValue = (float) maxValDict.optDouble("Total", 0);
                    maxYAxis = ((maxBarValue - minYAxis) > 5) ? maxBarValue : (maxBarValue + (maxBarValue * 2) / 100);

                } else {

                    for (int i = 0; i < values.length(); i++) {
                        JSONObject graphGroupValueDict = values.getJSONObject(i);
                        for (int j = 0; j < configurationArr.length(); j++) {
                            JSONObject configurationDict = configurationArr.getJSONObject(j);
                            String title = configurationDict.optString("title", "");

                            float barVal = (float)graphGroupValueDict.optDouble(title, 0);
                            if (barVal > maxBarVal) {
                                maxBarVal = barVal;
                            }
                            if (barVal < minBarVal) {
                                minBarVal = barVal;
                            }
                        }
                    }
                    maxYAxis = maxBarVal + maxBarVal / 50;
                    minYAxis = minBarVal - minBarVal / 50;
                }

                float yAxisOffset;
                yAxisOffset = (maxYAxis - minYAxis) / 4;


                // New Implementation goes here 29-12-2017
                if (maxBarVal == minBarVal) {
                    yAxisStrArr.add("0.0");
                    String minYAxisStr = String.format("%ld", (long) minYAxis);
                    if (minYAxisStr.length() >= 5) {
                        minYAxisStr = String.format("%ldk", (long) (minYAxis / 1000));

                    }
                    yAxisStrArr.add(minYAxisStr);
                } else {

                    if ((maxYAxis - minYAxis) > 5) {
                        for (int i = 0; i < 5; i++) {
                            String yAxisStr = String.format("%d", (int)(i * yAxisOffset + minYAxis));
                            if (yAxisStr.length() >= 5) {
                                yAxisStr = String.format("%dk", (int)(i * yAxisOffset + minYAxis) / 1000 );
                            }
                            yAxisStrArr.add(yAxisStr);
                        }

                    } else {
                        for (int i = 0; i < 5; i++) {
                            String yAxisStr = String.format("%.1f", i * yAxisOffset + minYAxis);
                            if (yAxisStr.length() >= 5) {
                                yAxisStr = String.format("%.1fk", (i * yAxisOffset + minYAxis) / 1000.0 );
                            }
                            yAxisStrArr.add(yAxisStr);
                        }
                    }
                }


                if (maxBarVal != 0) {
                    eachValHeight = yAxisHeight / (maxYAxis - minYAxis);

                    //========================================================
                    //=============   Configure y Axis View  =================
                    //========================================================

                    float yAxisUnitHeight = yAxisHeight / (yAxisStrArr.size() - 1);

                    Bitmap bitmapYAxis = Bitmap.createBitmap(
                            48, // Width
                            200, // Height
                            Bitmap.Config.ARGB_8888 // Config
                    );

                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

                    int screenWidth = displayMetrics.widthPixels;
                    int xBaseViewWidth = (int)Util.convertPixelsToDp(screenWidth) - 16 -48;

                    Bitmap bitmapXBase = Bitmap.createBitmap(
                            xBaseViewWidth, // Width
                            138, // Height
                            Bitmap.Config.ARGB_8888 // Config
                    );

                    // Initialize a new Canvas instance
                    Canvas canvasYAxis = new Canvas(bitmapYAxis);
                    Canvas canvasXBase = new Canvas(bitmapXBase);

                    Paint paint = new Paint();
                    paint.setColor(Color.BLACK);
                    paint.setStyle(Paint.Style.FILL);


                    for (int i = 0; i < yAxisStrArr.size(); i++) {

                        if (i == 0) { //yAxisStrArr.size() - 1) {
                            paint.setColor(ContextCompat.getColor(getContext(), R.color.red));
                            canvasYAxis.drawRect(39, 0, 45,  2, paint);
                            paint.setTextSize(10);
                            paint.setColor(Color.BLACK);
                            paint.setTextAlign(Paint.Align.RIGHT);
                            canvasYAxis.drawText(yAxisStrArr.get(yAxisStrArr.size() - 1 - i), 35, 10, paint);

                            paint.setColor(Color.LTGRAY);
                            canvasXBase.drawRect(0, 0, xBaseViewWidth, 2, paint);
                        } else {
                            paint.setColor(ContextCompat.getColor(getContext(), R.color.red));
                            canvasYAxis.drawRect(39, yAxisUnitHeight * i, 45, yAxisUnitHeight * i + 2, paint);
                            paint.setTextSize(10);
                            paint.setColor(Color.BLACK);
                            paint.setTextAlign(Paint.Align.RIGHT);
                            canvasYAxis.drawText(yAxisStrArr.get(yAxisStrArr.size() - 1 - i), 35, yAxisUnitHeight * i + 5, paint);

                            paint.setColor(Color.LTGRAY);
                            canvasXBase.drawRect(0, yAxisUnitHeight * i, xBaseViewWidth, yAxisUnitHeight * i + 2, paint);
                        }
                    }

                    yAxisView.setImageBitmap(bitmapYAxis);
                    xBaseView.setImageBitmap(bitmapXBase);



                    //========================================================
                    //=============   Draw Bar & Line Graph  =================
                    //========================================================

                    int scrollWidth = (int)(startOffset + (barGroupSpace + barGroupWidth) * values.length());
                    int scrollHeight = imgGraph.getLayoutParams().height;
                    scrollWidth = scrollWidth * scrollHeight / 200;
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(scrollWidth, imgGraph.getLayoutParams().height);
                    imgGraph.setLayoutParams(layoutParams);

                    Bitmap bitmapGraph = Bitmap.createBitmap(
                            scrollWidth * 200 / scrollHeight, // Width
                            200, // Height
                            Bitmap.Config.ARGB_8888 // Config
                    );
                    Canvas canvasGraph = new Canvas(bitmapGraph);

                    ArrayList<Float> prevValSumArr = new ArrayList<Float>();
                    for (int i = 0; i < values.length(); i++) {
                        prevValSumArr.add(new Float(0));
                    }
                    int columnIndex = 0;
                    for (int i = 0; i < configurationArr.length(); i++) {
                        JSONObject configurationDict = configurationArr.getJSONObject(i);
                        String title = configurationDict.optString("title", "");
                        String type = configurationDict.optString("type",  "");
                        int lineAlpha = (int)(configurationDict.optDouble("lineAlpha", 0) * 100);
                        int fillAlpha = (int)(100 * configurationDict.optDouble("fillAlphas", 0));

                        int fillColor;
                        String fillColorString = configurationDict.optString("fillColors", null);


                        if (fillColorString == null) {
                            Log.e("======", title);
                            fillColor = Integer.parseInt(graphColorArr.get(i));
                        } else {
                            Log.e("======", fillColorString);
                            fillColor = Color.parseColor(fillColorString);
                        }

                        int lineColor;
                        String lineColorString = configurationDict.optString("lineColor", null);
                        if (lineColorString == null) {
                            lineColor = Integer.parseInt(graphColorArr.get(i));
                        } else {
                            lineColor = Color.parseColor(lineColorString);
                        }

                        if (type.equals("column")) {
                            if (isStack) {
                                for (int j = values.length()-1; j >= 0 ; j--) {
                                    JSONObject barGroupDict = values.getJSONObject(j);
                                    float barVal = (float)barGroupDict.optDouble(title, 0);

                                    float sumVal = prevValSumArr.get(j);
                                    sumVal += barVal;
                                    prevValSumArr.set(j, new Float(sumVal));

                                    paint.setColor(fillColor);
                                    float left = startOffset + (values.length() - 1 - j) * (barGroupWidth + barGroupSpace);
                                    float top = yAxisHeight - sumVal * eachValHeight;
                                    float right = left + barWidth;
                                    float bottom = top + barVal * eachValHeight;

                                    canvasGraph.drawRect(left, top, right, bottom, paint);
                                }
                            } else {
                                for (int j = values.length()-1; j >= 0 ; j--) {
                                    JSONObject barGroupDict = values.getJSONObject(j);
                                    float barVal = (float)barGroupDict.optDouble(title, 0) - minYAxis;

                                    if (barVal < 0) {
                                        barVal = 0;
                                    }

                                    paint.setColor(fillColor);
                                    float left = startOffset + columnIndex * (barWidth + barSpace) + (values.length() - 1 - j) * (barGroupWidth + barGroupSpace);
                                    float top = yAxisHeight - barVal * eachValHeight;
                                    float right = left + barWidth;
                                    float bottom = top + barVal * eachValHeight;

                                    canvasGraph.drawRect(left, top, right, bottom, paint);
                                }
                                columnIndex ++;
                            }
                        } else {

                            ArrayList<Point> linePointArr = new ArrayList<Point>();
                            for (int j = values.length()-1; j >= 0 ; j--) {
                                JSONObject barGroupDict = values.getJSONObject(j);
                                float lineVal = (float)barGroupDict.optDouble(title, 0) - minYAxis;
                                if (lineVal < 0) {
                                    lineVal = 0;
                                }
                                int middlePosInBars = (int)(startOffset + (barGroupWidth + barGroupSpace) * (values.length() - 1 - j) + (barGroupWidth - barSpace) / 2.0f);

                                linePointArr.add(new Point(middlePosInBars, (int)(yAxisHeight - lineVal * eachValHeight)));
                            }

                            paint.setColor(lineColor);
                            paint.setStyle(Paint.Style.STROKE);
                            paint.setStrokeWidth(2);
                            float linePoints[] = new float[linePointArr.size() * 2];
                            for (int k = 1; k < linePointArr.size(); k++) {

                                Point start = linePointArr.get(k-1);
                                Point end = linePointArr.get(k);
                                canvasGraph.drawLine(start.x, start.y, end.x, end.y, paint);
                            }

                        }

                    }

                    //========================================================
                    //=====================   Draw Dot  ======================
                    //========================================================

                    for (int i = 0; i < configurationArr.length(); i++) {
                        JSONObject configurationDict = configurationArr.getJSONObject(i);
                        String title = configurationDict.optString("title", "");
                        String type = configurationDict.optString("type", "");

                        int lineColor;
                        String lineColorString = configurationDict.optString("lineColor", null);
                        if (lineColorString == null) {
                            lineColor = Integer.parseInt(graphColorArr.get(i));
                        } else {
                            lineColor = Color.parseColor(lineColorString);
                        }

                        if (type.equals("line")) {
                            for (int j = values.length()-1; j >= 0 ; j--) {
                                JSONObject barGroupDict = values.getJSONObject(j);
                                float lineVal = (float)barGroupDict.optDouble(title, 0) - minYAxis;
                                if (lineVal < 0) lineVal = 0;

                                float middlePosInBars = startOffset + (barGroupWidth + barGroupSpace) * (values.length() - j -1) + (barGroupWidth - barSpace) / 2;

                                paint.setStyle(Paint.Style.FILL);
                                paint.setColor(lineColor);
                                canvasGraph.drawCircle(middlePosInBars, yAxisHeight - lineVal * eachValHeight, 3.5f, paint);
                            }
                        }
                    }


                    //========================================================
                    //=====================   Draw xAxis  ======================
                    //========================================================

                    for (int i = values.length()-1; i >= 0 ; i--) {
                        JSONObject graphGroupValueDict = values.getJSONObject(i);

                        String dateStr = graphGroupValueDict.optString("ValueDateString", "");
                        float middlePosInBars = startOffset + (barGroupWidth + barGroupSpace) * (values.length() - i - 1) + (barGroupWidth - barSpace) / 2;

                        paint.setStyle(Paint.Style.FILL);
                        paint.setColor(Color.DKGRAY);
                        canvasGraph.drawRect(middlePosInBars - 1, yAxisHeight + 2, middlePosInBars + 1, yAxisHeight + 8, paint);

                        canvasGraph.save();
                        canvasGraph.rotate(-60, (float)(middlePosInBars+4), (float)(yAxisHeight + 15));
                        canvasGraph.drawText(dateStr, (float)(middlePosInBars+4), (float)(yAxisHeight + 15), paint);
                        canvasGraph.restore();
                    }

                    imgGraph.setImageBitmap(bitmapGraph);

                }

            } catch (Exception e) {

            }
        }
    }

}
