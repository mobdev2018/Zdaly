package mayah.zdalyapp.zdaly.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by hello on 2/7/18.
 */

public class SetImageFromURL extends AsyncTask<Void, Bitmap, Bitmap> {

    ImageView imageView;
    String strURL;
    Bitmap bitmap;

    public SetImageFromURL(ImageView imageView, String url) {
        this.imageView = imageView;
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
            imageView.setImageBitmap(result);
        }
    }
}