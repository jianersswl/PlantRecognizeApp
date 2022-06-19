package com.example.cameraalbumtest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static final int TAKE_PHOTO = 1;
    //从相册中选择照片
    public static final int CHOOSE_PHOTO = 2;
    private String reupload = "Reupload";
    private static final String TAG = "led";

    private String prediction;
    private String angle;

    static private String imagePath;
    static private ImageView imageBox;
    static private Uri imageUri;
    static private ProgressBar uploadProgress;
    static private String upload;
    static private Button uploadButton;
    static private TextView langleContent;
    static private TextView lpredictContent;
    static private TextView rangleContent;
    static private TextView rpredictContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button takePhoto = (Button)findViewById(R.id.take_photo);
        Button choosePhoto = (Button)findViewById(R.id.choose_from_album);
        imageBox = findViewById(R.id.image_box);
        uploadProgress = findViewById(R.id.upload_progress);
        uploadButton = findViewById(R.id.upload_button);
        upload = uploadButton.getText().toString();
        langleContent = findViewById(R.id.langleContent);
        lpredictContent = findViewById(R.id.lpredictContent);
        rangleContent = findViewById(R.id.rangleContent);
        rpredictContent = findViewById(R.id.rpredictContent);
        uploadProgress.setVisibility(View.GONE);

        choosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else {
                    openAlbum();
                }
            }
        });
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File outputImage = new File(getExternalCacheDir(),"output_image.jpg");//getExternalCacheDir可以得到应用关联缓存目录
                try {
                    if(outputImage.exists()){
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
                if(Build.VERSION.SDK_INT >= 24){
                    imageUri = FileProvider.getUriForFile(MainActivity.this,"com.example.cameraalbumtest.fileprovider",outputImage);
                }else {
                    imageUri = Uri.fromFile(outputImage);
                }
                //启动相机
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, TAKE_PHOTO);
            }
        });
    }

    private void openAlbum(){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_LONG).show();
                }
                break;
                default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case TAKE_PHOTO:
                if(resultCode == RESULT_OK){
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if(resultCode == RESULT_OK){
                    if(Build.VERSION.SDK_INT >= 19){
                        handleImageOnKitKat(data);
                    }else {
                        handleImageBeforeKitKat(data);
                    }
                }
                break;
            default:
                break;
        }
    }
    @TargetApi(19)
    private void handleImageOnKitKat(Intent data){
        String imagePath = null;
        Uri uri = data.getData();
        if(DocumentsContract.isDocumentUri(this, uri)){
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            imagePath = getImagePath(uri, null);
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            imagePath = uri.getPath();
        }
        this.imagePath = imagePath;
        displayImageLocal(imagePath);
    }

    private void handleImageBeforeKitKat(Intent data){
        Uri uri = data.getData();
        String imagePath = getImagePath(uri,null);
        this.imagePath = imagePath;
        displayImageLocal(imagePath);
    }

    private String getImagePath(Uri uri, String selection){
        String path = null;
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if(cursor != null){
            if(cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImageLocal(String imagePath){
        if(imagePath != null){
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            bitmap = ImageProcessing.resizeImage(bitmap, imageBox.getHeight(),imageBox.getWidth());
            imageBox.setImageBitmap(bitmap);
        }else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_LONG).show();
        }
    }

    public void uploadClick(View view){
        Log.e(TAG, ": onClick\n");
        // show progress
        if (uploadButton.getText().toString() == upload){
            uploadProgress.setVisibility(View.VISIBLE);
            uploadButton.setText(reupload);
            Connection connection = new Connection();
            connection.postRecognizeImageSync(imagePath, new ImageBoxHandler(), new TextViewHandler());
//            connection.postRecognizeUrlSync("https://8.129.217.181:443/media/origin/1.jpg", new ImageBoxHandler());
        }else{
            uploadProgress.setVisibility(View.GONE);
            uploadButton.setText(upload);
            imageBox.setImageResource(R.drawable.image1);
            langleContent.setText("0");
            lpredictContent.setText("no predict");
            rangleContent.setText("0");
            rpredictContent.setText("no predict");

            imagePath = "";
        }

    }

    public static class ImageBoxHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bitmap bitmap = (Bitmap)msg.obj;
            bitmap = ImageProcessing.resizeImage(bitmap, imageBox.getHeight(),imageBox.getWidth());
            imageBox.setImageBitmap(bitmap);
            uploadProgress.setVisibility(View.GONE);
            System.out.println("update imageView successfully!");
        }
    }

    public static class TextViewHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            langleContent.setText(msg.getData().getString("angleL")+"°");
            lpredictContent.setText(msg.getData().getString("predictionL"));
            rangleContent.setText(msg.getData().getString("angleR")+"°");
            rpredictContent.setText(msg.getData().getString("predictionR"));
            System.out.println("update textView successfully!");
        }
    }
}

