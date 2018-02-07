package mayah.zdalyapp.zdaly;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mayah.zdalyapp.zdaly.util.Constant;
import mayah.zdalyapp.zdaly.util.Util;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.txtName)
    EditText txtName;
    @BindView(R.id.txtPassword)
    EditText txtPassword;
    @BindView(R.id.imgTerms)
    ImageView imgTerms;
    @BindView(R.id.loadingView)
    RelativeLayout loadingView;
    @BindView(R.id.logoIv)
    ImageView logoIv;

    private boolean termAgreed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        SharedPreferences sharedPreferences = getSharedPreferences(Constant.SHARED_PR.SHARE_PREF, MODE_PRIVATE);
        String savedEmail = sharedPreferences.getString(Constant.SHARED_PR.KEY_EMAIL, "");
        String savedPassword = sharedPreferences.getString(Constant.SHARED_PR.KEY_PASSWORD, "");
        if (!savedEmail.equals("") && !savedPassword.equals("")) {
            txtName.setText(savedEmail);
            txtPassword.setText(savedPassword);
            doLogin();
        }
    }

    @OnClick(R.id.btnLogin)
    public void onLogin() {
        if (termAgreed && !getText(txtName).equals("") && !getText(txtPassword).equals("")) {
            this.doLogin();
        } else {
            toast("You need to agree with Zdaly terms fo usage in order to login.");
        }
    }

    @OnClick(R.id.viewCheck)
    public void onViewTerms() {
        termAgreed = !termAgreed;
        if (termAgreed) {
            imgTerms.setImageResource(R.drawable.check);
        } else {
            imgTerms.setImageResource(R.drawable.un_check);
        }
    }

    @OnClick(R.id.viewTermsofUsage)
    public void onViewTermsOfUsage () {
        String url = "https://www.zdaly.com/home/termsAndCondition";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }


    private void doLogin() {
        if (Util.isOnline(getApplicationContext())) {
            loadingView.setVisibility(View.VISIBLE);
            new Login(getText(txtName), getText(txtPassword)).execute();
        } else {
            toast(Constant.network_error);
        }
    }

    protected void toast(CharSequence text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    protected String getText(EditText eText) {
        return eText == null ? "" : eText.getText().toString().trim();
    }

    class Login extends AsyncTask<Void, String, String> {
        String email, password;
        String response;

        public Login(String email, String password) {
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
                JSONObject jData = new JSONObject();
                jData.put("email", email);
                jData.put("password", password);

                response = Util.postRequest(Constant.AUTHENTICATE_URL, jData.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                loadingView.animate()
                        .alpha(0.0f)
                        .setDuration(200)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationCancel(Animator animation) {
                                super.onAnimationCancel(animation);
                                loadingView.setVisibility(View.GONE);
                            }
                        });
                toast("Login failed and try again.");
                return;
            }
            try {
                JSONObject jObj = new JSONObject(result);
                String status = jObj.optString("state");

                if (status.equals("success")) {
                    String userId = jObj.optString("result");
                    String token = jObj.optString("token");

                    SharedPreferences sharedPreferences = getSharedPreferences(Constant.SHARED_PR.SHARE_PREF, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Constant.SHARED_PR.KEY_EMAIL, email);
                    editor.putString(Constant.SHARED_PR.KEY_PASSWORD, password);
                    editor.putString(Constant.SHARED_PR.KEY_ID, userId);
                    editor.apply();

                    String logoUrl = "http://74.63.228.198/DownloadCsv/" + userId + ".jpg";
                    new SetImageFromURL(logoUrl).execute();

                    loadingView.setVisibility(View.GONE);
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(i);

                } else {
                    loadingView.animate()
                            .alpha(0.0f)
                            .setDuration(200)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationCancel(Animator animation) {
                                    super.onAnimationCancel(animation);
                                    loadingView.setVisibility(View.GONE);
                                }
                            });
                    toast("Login failed and try again.");
                }

            } catch (JSONException e) {
                loadingView.setVisibility(View.GONE);
                e.printStackTrace();
            }
        }
    }

    class SetImageFromURL extends AsyncTask<Void, Bitmap, Bitmap> {
        String strURL;
        Bitmap bitmap;

        public SetImageFromURL(String url) {
            this.strURL = url;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL url = new URL(strURL);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();

                InputStream is = url.openStream(); //connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (result != null) {
                logoIv.setImageBitmap(result);
            }
        }
    }

}
