package me.iwf.photopicker.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import android.text.TextUtils;
import android.util.Log;

import androidx.core.content.FileProvider;
import androidx.core.os.EnvironmentCompat;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.iwf.photopicker.BuildConfig;

/**
 * Created by donglua on 15/6/23.
 * <p>
 * <p>
 * http://developer.android.com/training/camera/photobasics.html
 */
public class ImageCaptureManager {

    private final static String CAPTURED_PHOTO_PATH_KEY = "mCurrentPhotoPath";
    public static final int REQUEST_TAKE_PHOTO = 1;

    private String mCurrentPhotoPath;
    private int mCurrentPhotoID;
    private Context mContext;

    public ImageCaptureManager(Context mContext) {
        this.mContext = mContext;
    }

    // @NonNull
    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result.append(temp);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 创建图片地址uri,用于保存拍照后的照片 Android 10以后使用这种方法
     */
    private Uri createImageUri() {
        Log.d("ImageCaptureManager", "------>createImageUri");
        String time = Long.toString(System.currentTimeMillis());
        String name;
        String prename = md5(time);
        if (!prename.isEmpty()) {
            name = prename + ".jpg";
        } else {
            name = time + ".jpg";
        }

        Uri uriForFile;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DESCRIPTION, "JY_PICTURE");
        values.put(MediaStore.Images.Media.DISPLAY_NAME, name);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
        values.put(MediaStore.Images.Media.TITLE, name);
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
        //values.put(MediaStore.Images.Media.);
        //MainActivity.this.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES).getPath();
        Uri external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver resolver = this.mContext.getApplicationContext().getContentResolver();
        uriForFile = resolver.insert(external, values);

        String[] temp = uriForFile.toString().split("/");
        mCurrentPhotoID = Integer.parseInt(temp[temp.length - 1]);

        Log.d("ImageCaptureManager", "uri:" + uriForFile.toString());
        return uriForFile;

    }

    private File createImageFile() throws IOException {
//    // Create an image file name
//    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
//    String imageFileName = "JPEG_" + timeStamp + ".jpg";
//    File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//
//    if (!storageDir.exists()) {
//      if (!storageDir.mkdir()) {
//        Log.e("TAG", "Throwing Errors....");
//        throw new IOException();
//      }
//    }
//
//    File image = new File(storageDir, imageFileName);
//
//    // Save a file: path for use with ACTION_VIEW intents
//    mCurrentPhotoPath = image.getAbsolutePath();
//    return image;

        String imageName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        File tempFile = new File(storageDir, imageName);
        if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(tempFile))) {
            return null;
        }
        mCurrentPhotoPath = tempFile.getAbsolutePath();
        return tempFile;
    }


    public Intent dispatchTakePictureIntent() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
//        // Create the File where the photo should go
//        File file = createImageFile();
//        Uri photoFile;
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//          String authority = mContext.getApplicationInfo().packageName + ".provider";
//          photoFile = FileProvider.getUriForFile(this.mContext.getApplicationContext(), authority, file);
//        } else {
//          photoFile = Uri.fromFile(file);
//        }
//
//        // Continue only if the File was successfully created
//        if (photoFile != null) {
//          takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile);
//        }
            /////////////////////////////////////////////////////////////////////////////////////////
            File photoFile = null;
            Uri photoUri = null;

            if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // 适配android 10
                photoUri = createImageUri();
            } else {
                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (photoFile != null) {
                    //mCameraImagePath = photoFile.getAbsolutePath();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        //适配Android 7.0文件权限，通过FileProvider创建一个content类型的Uri
                        photoUri = FileProvider.getUriForFile(this.mContext.getApplicationContext(), mContext.getApplicationInfo().packageName + ".provider", photoFile);
                    } else {
                        photoUri = Uri.fromFile(photoFile);
                    }
                }
            }

            //mCameraUri = photoUri;
            if (photoUri != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                //startActivityForResult(captureIntent, CAMERA_REQUEST_CODE);
            }
        }
        return takePictureIntent;
    }

    /*
    刷新相册
     */
    public void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

        if (TextUtils.isEmpty(mCurrentPhotoPath)) {
            return;
        }

        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        mContext.sendBroadcast(mediaScanIntent);
    }


    public String getCurrentPhotoPath() {
        return mCurrentPhotoPath;
    }

    public int getCurrentPhotoID() {
        return mCurrentPhotoID;
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null && mCurrentPhotoPath != null) {
            savedInstanceState.putString(CAPTURED_PHOTO_PATH_KEY, mCurrentPhotoPath);
        }
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(CAPTURED_PHOTO_PATH_KEY)) {
            mCurrentPhotoPath = savedInstanceState.getString(CAPTURED_PHOTO_PATH_KEY);
        }
    }

}
