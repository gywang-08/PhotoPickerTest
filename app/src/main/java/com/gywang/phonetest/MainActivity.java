package com.gywang.phonetest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.PhotoPicker;
import me.iwf.photopicker.PhotoPreview;

public class MainActivity extends AppCompatActivity {

    Button pickPhoto;
    ArrayList<String> selectedPhotosPath = new ArrayList<>();
    ArrayList<Integer> selectedPhotosId = new ArrayList<>();
    int position = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pickPhoto = (Button)findViewById(R.id.pickphoto);
        pickPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PhotoPicker.builder()
                        .setPhotoCount(9)  //设置选择照片最大值，默认为9
                        .setGridColumnCount(4)  //设置在选择照片界面中的列数，默认为3
                        .setSelected(selectedPhotosPath)  //保存选择好的照片，下次选择照片在此集合的基础上添加
                        .setSelectedId(selectedPhotosId)
                        .setShowCamera(true)  //显示照相机按钮
                        .setShowGif(true)  //设置支持gif
                        .setPreviewEnabled(true)  //正在选择照片时是否可以预览
                        .start(MainActivity.this, PhotoPicker.REQUEST_CODE);
//                PhotoPreview.builder()
//                        .setPhotos(selectedPhotos)
//                        .setCurrentItem(position)  //预览照片在集合中的position
//                        .setShowDeleteButton(false)  //是否显示删除按钮
//                        .start(MainActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && (requestCode == PhotoPicker.REQUEST_CODE || requestCode == PhotoPreview.REQUEST_CODE)) {
            if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q){
                ArrayList<Integer> photos = null;
                if (data != null) {
                    photos = data.getIntegerArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS_ID);
                }
                selectedPhotosId.clear();
                if (photos != null) {
                    selectedPhotosId.addAll(photos);
                }
            }
            else {
                List<String> photos = null;
                if (data != null) {
                    photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS_PATH);
                }
                selectedPhotosPath.clear();
                if (photos != null) {
                    selectedPhotosPath.addAll(photos);
                }
            }
            //mPicAdapter.notifyDataSetChanged();
        }
    }
}
