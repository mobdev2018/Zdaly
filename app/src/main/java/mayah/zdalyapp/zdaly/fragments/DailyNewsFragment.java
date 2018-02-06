package mayah.zdalyapp.zdaly.fragments;

import android.app.FragmentTransaction;
import android.content.Context;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import mayah.zdalyapp.zdaly.MainActivity;
import mayah.zdalyapp.zdaly.R;
import mayah.zdalyapp.zdaly.util.Constant;
import mayah.zdalyapp.zdaly.util.Util;

public class DailyNewsFragment extends Fragment {

    @BindView(R.id.listview)
    ListView listView;
    @BindView(R.id.txtSearch)
    EditText txtSearch;
    @BindView(R.id.btnCancel)
    Button btnCancel;


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

        ((MainActivity)getActivity()).showLoadingDialog("Latest News..");
        new getDailyNews().execute();
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
        txtSearch.clearFocus();
        btnCancel.setVisibility(View.GONE);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    class getDailyNews extends AsyncTask<Void, String, String> {

        String response;

        public getDailyNews() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                response = Util.getRequest(Constant.DAILY_NEWS_URL + "?id=" + userid);
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
                    ((MainActivity)getActivity()).hideLoadingDialog();

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

                    listAdapter = new DailyNewsListAdapter(getActivity(), newsArr);
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
        JSONArray locallist;

        public DailyNewsListAdapter(Context context, JSONArray locallist) {
            this.mContext = context;
            this.locallist = locallist;
            inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        private void add(JSONObject object) {
            locallist.put(object);
        }

        @Override
        public int getCount() {
            return locallist.length();
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
                JSONObject object = (JSONObject) locallist.get(position);

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

            } catch (JSONException e) {
                e.printStackTrace();
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        JSONObject newsDict = isSearchEnabled ? searchedNewsArr.getJSONObject(position) : newsArr.getJSONObject(position);

                        DailyNewsWebViewFragment dailyNewsWebViewFragment = DailyNewsWebViewFragment.newInstance(newsDict);

                        final FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.listview, dailyNewsWebViewFragment, "DailyNewsWebViewFragmentTag");
                        ft.commit();

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
