package mayah.zdalyapp.zdaly;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mayah.zdalyapp.zdaly.fragments.KeyTrendsFragment;
import mayah.zdalyapp.zdaly.util.Util;

public class GraphShowActivity extends AppCompatActivity {

    public static JSONObject graphDict = new JSONObject();

    @BindView(R.id.viewGraph)
    View viewGraph;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.descView)
    LinearLayout descView;

    @BindView(R.id.yAxisView)
    ImageView yAxisView;
    @BindView(R.id.xBaseView)
    ImageView xBaseView;
    @BindView(R.id.imgGraph)
    ImageView imgGraph;


    JSONArray configurationArr;
    int columnCnt = 0;
    boolean isStack = false;

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_show);

        ButterKnife.bind(this);

        drawGraph();        
    }

    @OnClick(R.id.btnBack)
    public void onBack() {
        finish();
    }

    @OnClick(R.id.btnShare)
    public void onShare() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            share();
        }
    }

    private void share() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, getImageUri(this, getBitmapFromView(viewGraph)));
        shareIntent.setType("image/jpeg");
        try {
            startActivity(Intent.createChooser(shareIntent, "Share"));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                share();
            } else {
                // Permission Denied
                Toast.makeText(GraphShowActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void drawGraph() {
        showHeaderView();
        showGraph();
    }

    private void showHeaderView() {
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
                    fillColor = Integer.parseInt(KeyTrendsFragment.graphColorArr.get(i));
                } else {
                    fillColor = Color.parseColor(fillColorString);
                }

                int lineColor;
                String lineColorString = configurationDict.optString("lineColor", null);
                if (lineColorString == null) {
                    lineColor = Integer.parseInt(KeyTrendsFragment.graphColorArr.get(i));
                } else {
                    lineColor = Color.parseColor(lineColorString);
                }

                LinearLayout barDescGroup;
                int tag = i / 2;
                if ((i % 2) == 0) {
                    barDescGroup = new LinearLayout(this);
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
                LinearLayout barDescView = new LinearLayout(this);
                barDescView.setOrientation(LinearLayout.HORIZONTAL);
                barDescView.setLayoutParams(barDescViewParams);
                barDescView.setGravity(Gravity.CENTER_VERTICAL);

                if (type.equals("column")) {
                    LinearLayout.LayoutParams columnParams = new LinearLayout.LayoutParams(100, 40);
                    columnParams.rightMargin = 8;
                    View columnView = new View(this);
                    columnView.setLayoutParams(columnParams);
                    columnView.setBackgroundColor(fillColor);

                    barDescView.addView(columnView);

                } else {
                    RelativeLayout.LayoutParams lineViewParams = new RelativeLayout.LayoutParams(100, 40);
                    RelativeLayout lineView = new RelativeLayout(this);
                    lineView.setLayoutParams(lineViewParams);

                    RelativeLayout.LayoutParams lineParams = new RelativeLayout.LayoutParams(100, 3);
                    lineParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                    LinearLayout line = new LinearLayout(this);
                    line.setLayoutParams(lineParams);
                    line.setBackgroundColor(lineColor);
                    lineView.addView(line);

                    RelativeLayout.LayoutParams circleParam = new RelativeLayout.LayoutParams(20, 20);
                    circleParam.addRule(RelativeLayout.CENTER_IN_PARENT);
                    TextView dot = new TextView(this);
                    dot.setLayoutParams(circleParam);
                    dot.setBackgroundColor(lineColor);
                    dot.setBackground(Util.drawCircle(this, 20, 20, lineColor));
                    lineView.addView(dot);
                    barDescView.addView(lineView);
                }

                LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                TextView txtDesc = new TextView(this);
                txtDesc.setLayoutParams(descParams);
                txtDesc.setTextColor(ContextCompat.getColor(this, R.color.darkGray));
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
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

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
                        paint.setColor(ContextCompat.getColor(this, R.color.red));
                        canvasYAxis.drawRect(39, 0, 45,  2, paint);
                        paint.setTextSize(10);
                        paint.setColor(Color.BLACK);
                        paint.setTextAlign(Paint.Align.RIGHT);
                        canvasYAxis.drawText(yAxisStrArr.get(yAxisStrArr.size() - 1 - i), 35, 10, paint);

                        paint.setColor(Color.LTGRAY);
                        canvasXBase.drawRect(0, 0, xBaseViewWidth, 2, paint);
                    } else {
                        paint.setColor(ContextCompat.getColor(this, R.color.red));
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
                Log.e("============", String.format("%d, %d", scrollWidth, scrollHeight));
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
                        fillColor = Integer.parseInt(KeyTrendsFragment.graphColorArr.get(i));
                    } else {
                        fillColor = Color.parseColor(fillColorString);
                    }

                    int lineColor;
                    String lineColorString = configurationDict.optString("lineColor", null);
                    if (lineColorString == null) {
                        lineColor = Integer.parseInt(KeyTrendsFragment.graphColorArr.get(i));
                    } else {
                        lineColor = Color.parseColor(lineColorString);
                    }

                    if (type.equals("column")) {
                        if (isStack) {
                            for (int j = values.length() - 1; j >= 0; j--) {
                                JSONObject barGroupDict = values.getJSONObject(j);
                                float barVal = (float)barGroupDict.optDouble(title, 0);

                                float sumVal = prevValSumArr.get(j);
                                sumVal += barVal;
                                prevValSumArr.set(j, new Float(sumVal));

                                paint.setColor(fillColor);
                                float left = startOffset + (values.length() - j - 1) * (barGroupWidth + barGroupSpace);
                                float top = yAxisHeight - sumVal * eachValHeight;
                                float right = left + barWidth;
                                float bottom = top + barVal * eachValHeight;

                                canvasGraph.drawRect(left, top, right, bottom, paint);
                            }
                        } else {
                            for (int j = values.length() - 1; j >= 0; j--) {
                                JSONObject barGroupDict = values.getJSONObject(j);
                                float barVal = (float)barGroupDict.optDouble(title, 0) - minYAxis;

                                if (barVal < 0) {
                                    barVal = 0;
                                }

                                paint.setColor(fillColor);
                                float left = startOffset + columnIndex * (barWidth + barSpace) + (values.length() - j - 1) * (barGroupWidth + barGroupSpace);
                                float top = yAxisHeight - barVal * eachValHeight;
                                float right = left + barWidth;
                                float bottom = top + barVal * eachValHeight;

                                canvasGraph.drawRect(left, top, right, bottom, paint);
                            }
                            columnIndex ++;
                        }
                    } else {

                        ArrayList<Point> linePointArr = new ArrayList<Point>();
                        for (int j = values.length() - 1; j >= 0; j--) {
                            JSONObject barGroupDict = values.getJSONObject(j);
                            float lineVal = (float)barGroupDict.optDouble(title, 0) - minYAxis;
                            if (lineVal < 0) {
                                lineVal = 0;
                            }
                            int middlePosInBars = (int)(startOffset + (barGroupWidth + barGroupSpace) * (values.length() - j - 1) + (barGroupWidth - barSpace) / 2.0f);

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
                        lineColor = Integer.parseInt(KeyTrendsFragment.graphColorArr.get(i));
                    } else {
                        lineColor = Color.parseColor(lineColorString);
                    }

                    if (type.equals("line")) {
                        for (int j = values.length() - 1; j >= 0; j--) {
                            JSONObject barGroupDict = values.getJSONObject(j);
                            float lineVal = (float)barGroupDict.optDouble(title, 0) - minYAxis;
                            if (lineVal < 0) lineVal = 0;

                            float middlePosInBars = startOffset + (barGroupWidth + barGroupSpace) * (values.length() - j - 1) + (barGroupWidth - barSpace) / 2;

                            paint.setStyle(Paint.Style.FILL);
                            paint.setColor(lineColor);
                            canvasGraph.drawCircle(middlePosInBars, yAxisHeight - lineVal * eachValHeight, 3.5f, paint);
                        }
                    }
                }


                //========================================================
                //=====================   Draw xAxis  ======================
                //========================================================

                for (int i = 0; i < values.length(); i++) {
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
