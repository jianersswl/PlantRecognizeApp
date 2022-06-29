package com.example.cameraalbumtest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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

    private String root = "https://www.jianersswlq.top/";
    private String resultUrl = "static/recognize/flower/yueji.jpg";
    private String csrfUrl = "recognize/get_csrf";
    private String CSRF;
    private String response;
    private InputStream resultStream;
    private String code;
    private String msg;
    private String leaf;
    private String stalk;
    private String fruit;
    private String age;

    // 尝试手动认证CSRF，但是失败了，取消了django的全局csrf认证功能。
    // 未解决 CSRF校验问题
    // 思路： 先django后台生成CSRF
    public void getCSRF(){
        Request request = new Request.Builder().url(root + csrfUrl).get().build();
        Call call = okHttpClient.newCall(request);
        try {
            Log.i(TAG, ": Try connection:" + root + csrfUrl + "\n");
            String respon = call.execute().body().string();
            parseJsonWithCSRF(respon);
            Log.i(TAG, "GetCSRF: " + respon);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 通过url从服务器获取图片
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


    // 向服务发送图片的url
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
                    parseJsonWithPrediction(response);
                    getResultImageByUrlSync(imageBoxHandler);

                    Message msg = new Message();
                    msg.what = 1;
                    Bundle bundle = new Bundle();
                    bundle.putString("angleL", leaf);
                    bundle.putString("predictionL", stalk);
                    bundle.putString("angleR", fruit);
                    bundle.putString("predictionR", age);
                    msg.setData(bundle);
                    textViewHandler.sendMessage(msg);
                    Log.i(TAG, "Update TextView: " + "angle and prediction");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    // 向服务器发送图片并获得prediciton
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

                Request request = new Request
                        .Builder().url(root + "recognize/uploadImage")
                        .post(requestBody.build())
                        .build();

                Call call = okHttpClient.newCall(request);
                try {
                    Log.e(TAG, ": Try connection: " + root + "recognize/uploadImage" + "\n");
                    response = call.execute().body().string();
                    System.out.println("PostRecognizeImageSync: " + response);
                    Log.i(TAG, ": Finish connection\n");
                    parseJsonWithPrediction(response);
                    getResultImageByUrlSync(imageBoxHandler);

                    Message msg = new Message();
                    msg.what = 1;
                    Bundle bundle = new Bundle();
                    bundle.putString("angleL", leaf);
                    bundle.putString("predictionL", stalk);
                    bundle.putString("angleR", fruit);
                    bundle.putString("predictionR", age);
                    msg.setData(bundle);
                    textViewHandler.sendMessage(msg);
                    Log.i(TAG, "Update TextView: " + "angle and prediction");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    // 解析prediction的json文件
    private void parseJsonWithPrediction(String response) throws IOException {
        try{
            JSONObject jsonObject =new JSONObject(response);
            code = jsonObject.getString("code");
            msg = jsonObject.getString("msg");
            JSONObject plantInfo = jsonObject.getJSONObject("PlantInfo");
            resultUrl =plantInfo.getString("PlantResultURL");
            leaf =plantInfo.getString("leaf");
            stalk = plantInfo.getString("stalk");
            fruit =plantInfo.getString("fruit");
            age = plantInfo.getString("age");
        } catch ( JSONException e) {
            e.printStackTrace();
        }
    }

    // 解析CSRF的json文件
    private void parseJsonWithCSRF(String response) throws IOException {
        try{
            JSONObject jsonObject =new JSONObject(response);
            CSRF = jsonObject.getString("CSRF");
        } catch ( JSONException e) {
            e.printStackTrace();
        }
    }

}