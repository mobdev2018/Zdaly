package mayah.zdalyapp.zdaly.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import mayah.zdalyapp.zdaly.MainActivity;
import mayah.zdalyapp.zdaly.R;
import mayah.zdalyapp.zdaly.util.Constant;
import mayah.zdalyapp.zdaly.util.Util;

public class DailyNewsFragment extends Fragment {

    public static final int FILTER_NONE = 0;
    public static final int FILTER_DATE = 1;
    public static final int FILTER_SEARCH = 2;

    @BindView(R.id.listview)
    ListView listView;
    @BindView(R.id.txtSearch)
    EditText txtSearch;
    @BindView(R.id.btnCancel)
    Button btnCancel;
    @BindView(R.id.btnPrev)
    Button btnPrev;
    @BindView(R.id.btnNext)
    Button btnNext;
    @BindView(R.id.btnShareSelectedNews)
    Button btnShareSelectedNews;
    @BindView(R.id.selectAllView)
    View selectAllView;
    @BindView(R.id.imgSelectAll)
    ImageView imgSelectAll;
    @BindView(R.id.btnHome)
    Button btnHome;

    String userid;
    boolean isSearchEnabled = false;
    JSONArray newsArr;
    JSONArray searchedNewsArr, searchedNewsToShareArr;

    DailyNewsListAdapter listAdapter;

    public static DailyNewsFragment newInstance() {
        DailyNewsFragment fragment = new DailyNewsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily_news, container, false);
        ButterKnife.bind(this, view);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constant.SHARED_PR.SHARE_PREF, Context.MODE_PRIVATE);
        userid = sharedPreferences.getString(Constant.SHARED_PR.KEY_ID, "");

        newsArr = new JSONArray();
        searchedNewsArr = new JSONArray();
        searchedNewsToShareArr = new JSONArray();

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        apiCallToGetDailyNewsData();
    }

    @OnFocusChange(R.id.txtSearch)
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            btnCancel.setVisibility(View.VISIBLE);
        } else {
            btnCancel.setVisibility(View.GONE);
        }

    }

    @OnClick(R.id.btnCancel)
    public void onCancel() {
        txtSearch.setText("");
        txtSearch.clearFocus();
        btnCancel.setVisibility(View.GONE);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        hideSelectAllViewFromBottomBar();
        apiCallToGetDailyNewsData();
    }

    @OnClick(R.id.selectAllView)
    public void onSelectAllView() {
        if (selectAllView.getTag() != null && ((int)selectAllView.getTag()) == 1) {
            selectAllView.setTag(0);
            imgSelectAll.setImageBitmap(null);
            searchedNewsToShareArr = new JSONArray();
        } else {
            selectAllView.setTag(1);
            imgSelectAll.setImageResource(R.drawable.icon_check);
            searchedNewsToShareArr = new JSONArray();
            searchedNewsToShareArr = searchedNewsArr;
        }

        listAdapter = new DailyNewsListAdapter(getActivity());
        listView.setAdapter(listAdapter);
    }

    @OnClick(R.id.btnShareSelectedNews)
    public void onShareSelectedSearchNews() {
        if (searchedNewsToShareArr.length() > 0) {
            JSONArray activityItems = new JSONArray();
//            activityItems.put()










            
        }
    }

    @OnClick(R.id.btnHome)
    public void onHome() {
        apiCallToGetDailyNewsData();
    }

    @OnClick(R.id.btnNext)
    public void onNext() {
        int tag = (int)btnNext.getTag();
        if (tag != 0) {
            apiCallToGetFilteredNewsOf_date(getPreviousAndNextDateBy(tag));
            btnPrev.setTag(tag);
            btnNext.setTag(tag-1);
        } else {
            apiCallToGetDailyNewsData();
        }
    }

    @OnClick(R.id.btnPrev)
    public void onPrev() {
        int tag = (int)btnPrev.getTag();
        btnPrev.setTag(tag+1);
        btnNext.setTag(tag-1);
        apiCallToGetFilteredNewsOf_date(getPreviousAndNextDateBy(tag+1));
    }

    private String getPreviousAndNextDateBy(int dayCount) {
        Calendar calendar = Calendar.getInstance();
        Date currentTime = calendar.getTime();
        calendar.setTime(currentTime);
        calendar.add(Calendar.DAY_OF_YEAR, -dayCount);
        Date newDate = calendar.getTime();
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String date = dateFormat.format(newDate).toString();
        return date;
    }

    private void hideSelectAllViewFromBottomBar() {
        btnNext.setVisibility(View.VISIBLE);
        btnPrev.setVisibility(View.VISIBLE);
        btnHome.setVisibility(View.VISIBLE);
        btnShareSelectedNews.setVisibility(View.GONE);
        selectAllView.setVisibility(View.GONE);
        initializeSearchCollections();
    }

    private void initializeSearchCollections() {
        searchedNewsArr = new JSONArray();
        searchedNewsToShareArr = new JSONArray();
    }

    private void apiCallToGetDailyNewsData() {
        btnPrev.setTag(0);
        btnNext.setTag(0);

        ((MainActivity)getActivity()).showLoadingDialog("Latest News..");
        new getDailyNews().execute();
    }

    private void apiCallToGetFilteredNewsOf_date(String date) {
        ((MainActivity)getActivity()).showLoadingDialog("Getting news on " + date);
        new getDailyNews(FILTER_DATE, date).execute();
    }

    class getDailyNews extends AsyncTask<Void, String, String> {

        String date = "";
        String search = "";
        int filter = FILTER_NONE;
        String response;

        public getDailyNews() {
            filter = FILTER_NONE;
            this.date = "";
            this.search = "";
        }

        public getDailyNews(int filter, String value) {
            this.filter = filter;
            if (filter == FILTER_DATE) {
                this.date = value;
            } else if (filter == FILTER_SEARCH) {
                this.search = value;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                switch (filter) {
                    case FILTER_NONE:
                        response = Util.getRequest(Constant.DAILY_NEWS_URL + "?id=" + userid);
                        break;
                    case FILTER_DATE:
                        response = Util.getRequest(Constant.DAILY_NEWS_URL + "?id=" + userid + "&date=" + date);
                        break;
                    case FILTER_SEARCH:
                        response = Util.getRequest(Constant.DAILY_NEWS_URL + "?id=" + userid + "&search=" + search);
                        break;
                    default:
                        response = Util.getRequest(Constant.DAILY_NEWS_URL + "?id=" + userid);
                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.e("================", result);
            try {
                JSONObject jObj = new JSONObject(result);
                String status = jObj.optString("state");

                if (status.equals("success")) {

                    if (isSearchEnabled) {
                        searchedNewsArr = new JSONArray();
                        searchedNewsToShareArr = new JSONArray();

                        if (jObj.opt("result").getClass() == JSONArray.class) {
                            searchedNewsArr.put(jObj.optJSONArray("result"));
                        } else {
                            toast("Sorry! No data found.");
                        }

                    } else {
                        newsArr = jObj.optJSONArray("result");
                    }

                    listAdapter = new DailyNewsListAdapter(getActivity());
                    listView.setAdapter(listAdapter);
                } else {
                    ((MainActivity)getActivity()).hideLoadingDialog();
                    toast("Sorry! No data found.");
                }

            } catch (JSONException e) {
                ((MainActivity)getActivity()).hideLoadingDialog();
                toast("Sorry! No data found.");
            }
        }
    }

    public class DailyNewsListAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater inflater = null;

        public DailyNewsListAdapter(Context context) {
            this.mContext = context;
            inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        private void add(JSONObject object) {
            newsArr.put(object);
        }

        @Override
        public int getCount() {
            return newsArr.length();
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
        public View getView(final int position, View view, ViewGroup viewGroup) {

            ViewHolder holder;
            view = inflater.inflate(R.layout.row_daily_news, null);
            holder = new ViewHolder(view);
            view.setTag(holder);

            try {
                final JSONObject object = (JSONObject) newsArr.get(position);

                holder.txtTitle.setText(object.optString("TITLE", ""));
                holder.txtContent.setText(object.optString("DES", ""));
                holder.txtSource.setText(object.optString("SOURCE", ""));
                holder.txtDate.setText(object.optString("DATE", ""));

                if (isSearchEnabled) {
                    holder.btnSelectCheck.setVisibility(View.GONE);
                    holder.txtDate.setVisibility(View.GONE);
                    holder.btnShare.setVisibility(View.GONE);

                    if (searchedNewsToShareArr.length() > 0) {
                        holder.btnSelectCheck.setBackgroundResource(R.drawable.icon_check);
                    } else {
                        holder.btnSelectCheck.setVisibility(View.GONE);
                    }
                } else {
                    holder.btnSelectCheck.setVisibility(View.VISIBLE);
                    holder.txtDate.setVisibility(View.VISIBLE);
                }

                holder.btnShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String title  = object.optString("TITLE", "");
                        String link = object.optString("LINK", "");

                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
                        shareIntent.putExtra(Intent.EXTRA_TEXT, link);

                        startActivity(Intent.createChooser(shareIntent, "Share"));

                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        JSONObject newsDict = isSearchEnabled ? searchedNewsArr.getJSONObject(position) : newsArr.getJSONObject(position);

                        ((MainActivity)getActivity()).showDailyNewsWebView(newsDict);

                    } catch (JSONException e) {

                    }

                }
            });

            return view;
        }

    }


    static class ViewHolder {

        @BindView(R.id.txtTitle)
        TextView txtTitle;
        @BindView(R.id.txtContent)
        TextView txtContent;
        @BindView(R.id.txtSource)
        TextView txtSource;
        @BindView(R.id.txtDate)
        TextView txtDate;
        @BindView(R.id.btnSelectCheck)
        Button btnSelectCheck;
        @BindView(R.id.btnShare)
        Button btnShare;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    private void toast(CharSequence text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

}
