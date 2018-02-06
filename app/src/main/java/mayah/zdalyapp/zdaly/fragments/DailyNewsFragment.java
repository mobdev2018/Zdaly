package mayah.zdalyapp.zdaly.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import mayah.zdalyapp.zdaly.LoginActivity;
import mayah.zdalyapp.zdaly.MainActivity;
import mayah.zdalyapp.zdaly.R;
import mayah.zdalyapp.zdaly.util.Constant;
import mayah.zdalyapp.zdaly.util.Util;

public class DailyNewsFragment extends Fragment {

    String userid;

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

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((MainActivity)getActivity()).showLoadingDialog("Latest News..");
        new getDailyNews().execute();
    }


    class getDailyNews extends AsyncTask<Void, String, String> {
        String email, password;
        String response;

        public getDailyNews() {
            this.email = email;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                Log.d("userid=====", userid);
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
                    String userId = jObj.optString("result");
                    String token = jObj.optString("token");



                } else {

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }



    static class ViewHolder {


        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }




}
