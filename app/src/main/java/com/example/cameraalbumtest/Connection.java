package com.example.cameraalbumtest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Connection {
    private static final String TAG = "led";
    private OkHttpClient okHttpClient = new OkHttpsClient().getOkHttpClient();

    private String root = "https://8.129.217.181:443/";
    private String resultUrl;
    private String response;
    private InputStream resultStream;
    private String code;
    private String msg;
    private String angleL;
    private String predictionL;
    private String angleR;
    private String predictionR;
    public Connection() {}

    public Connection(String url) {
        root = url;
    }

    public void getResultImageByUrlSync(final Handler imageBoxHandler){

        new Thread() {
            @Override
            public void run() {
                Request request = new Request.Builder().url(root + resultUrl).get().build();
                Call call = okHttpClient.newCall(request);
                try {
                    Log.i(TAG, ": Try connection:" + root + resultUrl + "\n");
                    Response respon = call.execute();
                    resultStream = respon.body().byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(resultStream);
                    Log.i(TAG, "GetResultImageByUrlSync: " + respon);

                    Message msg1 = Message.obtain();
                    msg1.obj = bitmap;
                    msg1.what = 1;
                    imageBoxHandler.sendMessage(msg1);

                    Log.i(TAG, "Update ImageView: " + msg1.obj);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }



    public void postRecognizeUrlSync(String url, final Handler imageBoxHandler, final Handler textViewHandler ) {
        final String urlf = url;
        new Thread() {
            @Override
            public void run() {
                FormBody formBody = new FormBody.Builder()
                        .add("leftAnkleURL", "None")
                        .add("rightAnkleURL", "None")
                        .add("doubleAnkleURL", ""+urlf)
                        .build();
                Request request = new Request.Builder().url(root + "recognize/uploadUrl").post(formBody).build();
                Call call = okHttpClient.newCall(request);
                try {
                    Log.i(TAG, ": Try connection: " + root + "recognize/uploadUrl" + "\n");
                    response = call.execute().body().string();
                    System.out.println("PostRecognizeUrlSync: " + response);
                    Log.i(TAG, ": Finish connection\n");
                    parseJsonWithJsonObject(response);
                    getResultImageByUrlSync(imageBoxHandler);

                    Message msg = new Message();
                    msg.what = 1;
                    Bundle bundle = new Bundle();
                    bundle.putString("angleL", angleL);
                    bundle.putString("predictionL",predictionL);
                    bundle.putString("angleR", angleR);
                    bundle.putString("predictionR",predictionR);
                    msg.setData(bundle);
                    textViewHandler.sendMessage(msg);
                    Log.i(TAG, "Update TextView: " + "angle and prediction");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void postRecognizeImageSync(String filePath,final Handler imageBoxHandler, final Handler textViewHandler) {
        final String filePathf = filePath;
        new Thread() {
            @Override
            public void run() {
                File image = new File("" + filePathf);
                System.out.println("File size: " + image.length());

                MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
                if (image != null) {
                    RequestBody body = RequestBody.create(image, MediaType.parse("image/jpeg"));
                    String filename = image.getName();
                    requestBody.addFormDataPart("image", filename, body).build();
                }

                Request request = new Request.Builder().url(root + "recognize/uploadImage").post(requestBody.build()).build();
                Call call = okHttpClient.newCall(request);
                try {
                    Log.i(TAG, ": Try connection: " + root + "recognize/uploadImage" + "\n");
                    response = call.execute().body().string();
                    System.out.println("PostRecognizeImageSync: " + response);
                    Log.i(TAG, ": Finish connection\n");
                    parseJsonWithJsonObject(response);
                    getResultImageByUrlSync(imageBoxHandler);

                    Message msg = new Message();
                    msg.what = 1;
                    Bundle bundle = new Bundle();
                    bundle.putString("angleL", angleL);
                    bundle.putString("predictionL",predictionL);
                    bundle.putString("angleR", angleR);
                    bundle.putString("predictionR",predictionR);
                    msg.setData(bundle);
                    textViewHandler.sendMessage(msg);
                    Log.i(TAG, "Update TextView: " + "angle and prediction");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void parseJsonWithJsonObject(String response) throws IOException {
        try{
            JSONObject jsonObject =new JSONObject(response);
            code = jsonObject.getString("code");
            msg = jsonObject.getString("msg");
            JSONObject ankleInfo = jsonObject.getJSONObject("AnkleInfo");
            resultUrl =ankleInfo.getString("AnkleResultURL");
            JSONObject leftAnkleInfo = ankleInfo.getJSONObject("leftAnkleInfo");
            angleL =leftAnkleInfo.getString("angle");
            predictionL = leftAnkleInfo.getString("prediction");
            JSONObject rightAnkleInfo = ankleInfo.getJSONObject("rightAnkleInfo");
            angleR =rightAnkleInfo.getString("angle");
            predictionR = rightAnkleInfo.getString("prediction");
        } catch ( JSONException e) {
            e.printStackTrace();
        }
    }

}