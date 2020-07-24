package me.iwf.photopicker;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.entity.Photo;
import me.iwf.photopicker.event.OnItemCheckListener;
import me.iwf.photopicker.fragment.ImagePagerFragment;
import me.iwf.photopicker.fragment.PhotoPickerFragment;

import static android.widget.Toast.LENGTH_LONG;
import static me.iwf.photopicker.PhotoPicker.DEFAULT_COLUMN_NUMBER;
import static me.iwf.photopicker.PhotoPicker.DEFAULT_MAX_COUNT;
import static me.iwf.photopicker.PhotoPicker.EXTRA_GRID_COLUMN;
import static me.iwf.photopicker.PhotoPicker.EXTRA_MAX_COUNT;
import static me.iwf.photopicker.PhotoPicker.EXTRA_ORIGINAL_PHOTOS_ID;
import static me.iwf.photopicker.PhotoPicker.EXTRA_ORIGINAL_PHOTOS_PATH;
import static me.iwf.photopicker.PhotoPicker.EXTRA_PREVIEW_ENABLED;
import static me.iwf.photopicker.PhotoPicker.EXTRA_SHOW_CAMERA;
import static me.iwf.photopicker.PhotoPicker.EXTRA_SHOW_GIF;
import static me.iwf.photopicker.PhotoPicker.KEY_SELECTED_PHOTOS_PATH;
import static me.iwf.photopicker.PhotoPicker.KEY_SELECTED_PHOTOS_ID;

public class PhotoPickerActivity extends AppCompatActivity {

    private PhotoPickerFragment pickerFragment;
    private ImagePagerFragment imagePagerFragment;
    private MenuItem menuDoneItem;

    private int maxCount = DEFAULT_MAX_COUNT;

    /**
     * to prevent multiple calls to inflate menu
     */
    private boolean menuIsInflated = false;

    private boolean showGif = false;
    private ArrayList<String> originalPhotosPath = null;
    private ArrayList<Integer> originalPhotosId = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean showCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, true);
        boolean showGif = getIntent().getBooleanExtra(EXTRA_SHOW_GIF, false);
        boolean previewEnabled = getIntent().getBooleanExtra(EXTRA_PREVIEW_ENABLED, true);

        setShowGif(showGif);

        setContentView(R.layout.__picker_activity_photo_picker);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        setTitle(R.string.__picker_title);

        ActionBar actionBar = getSupportActionBar();

        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            actionBar.setElevation(25);
        }

        maxCount = getIntent().getIntExtra(EXTRA_MAX_COUNT, DEFAULT_MAX_COUNT);
        int columnNumber = getIntent().getIntExtra(EXTRA_GRID_COLUMN, DEFAULT_COLUMN_NUMBER);
        originalPhotosPath = getIntent().getStringArrayListExtra(EXTRA_ORIGINAL_PHOTOS_PATH);
        originalPhotosId = getIntent().getIntegerArrayListExtra(EXTRA_ORIGINAL_PHOTOS_ID);

        pickerFragment = (PhotoPickerFragment) getSupportFragmentManager().findFragmentByTag("tag");
        if (pickerFragment == null) {
            pickerFragment = PhotoPickerFragment
                    .newInstance(showCamera, showGif, previewEnabled, columnNumber, maxCount, originalPhotosPath, originalPhotosId);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, pickerFragment, "tag")
                    .commit();
            getSupportFragmentManager().executePendingTransactions();
        }

        pickerFragment.getPhotoGridAdapter().setOnItemCheckListener(new OnItemCheckListener() {
            @Override
            public boolean onItemCheck(int position, Photo photo, final int selectedItemCount) {

                menuDoneItem.setEnabled(selectedItemCount > 0);

                if (maxCount <= 1) {
                    List<String> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotosPath();
                    if (!photos.contains(photo.getPath())) {
                        photos.clear();
                        pickerFragment.getPhotoGridAdapter().notifyDataSetChanged();
                    }
                    return true;
                }

                if (selectedItemCount > maxCount) {
                    Toast.makeText(getActivity(), getString(R.string.__picker_over_max_count_tips, maxCount),
                            LENGTH_LONG).show();
                    return false;
                }
                if (maxCount > 1) {
                    menuDoneItem.setTitle(getString(R.string.__picker_done_with_count, selectedItemCount, maxCount));
                } else {
                    menuDoneItem.setTitle(getString(R.string.__picker_done));
                }
                return true;
            }
        });

    }



    //刷新右上角按钮文案
    public void updateTitleDoneItem() {
        if (menuIsInflated) {
            if (pickerFragment != null && pickerFragment.isResumed()) {
                int size = 0;// = photos == null ? 0 : photos.size();
                if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q)
                {
                    List<Integer> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotoId();
                    size = photos == null ? 0 : photos.size();
                } else {
                    List<String> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotosPath();
                    size = photos == null ? 0 : photos.size();
                }

                menuDoneItem.setEnabled(size > 0);
                if (maxCount > 1) {
                    menuDoneItem.setTitle(getString(R.string.__picker_done_with_count, size, maxCount));
                } else {
                    menuDoneItem.setTitle(getString(R.string.__picker_done));
                }

            } else if (imagePagerFragment != null && imagePagerFragment.isResumed()) {
                //预览界面 完成总是可点的，没选就把默认当前图片
                menuDoneItem.setEnabled(true);
            }

        }
    }

    /**
     * Overriding this method allows us to run our exit animation first, then exiting
     * the activity when it complete.
     */
    @Override
    public void onBackPressed() {
        if (imagePagerFragment != null && imagePagerFragment.isVisible()) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            }
        } else {
            super.onBackPressed();
        }
    }


    public void addImagePagerFragment(ImagePagerFragment imagePagerFragment) {
        this.imagePagerFragment = imagePagerFragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, this.imagePagerFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!menuIsInflated) {
            getMenuInflater().inflate(R.menu.__picker_menu_picker, menu);
            menuDoneItem = menu.findItem(R.id.done);
            if (originalPhotosPath != null && originalPhotosPath.size() > 0) {
                menuDoneItem.setEnabled(true);
                menuDoneItem.setTitle(
                        getString(R.string.__picker_done_with_count, originalPhotosPath.size(), maxCount));
            } else {
                menuDoneItem.setEnabled(false);
            }
            menuIsInflated = true;
            return true;
        }
        return false;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }

        if (item.getItemId() == R.id.done) {
            Intent intent = new Intent();
            if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q){
                ArrayList<Integer> selectedPhotosid = null;
                if (pickerFragment != null) {
                    selectedPhotosid = pickerFragment.getPhotoGridAdapter().getSelectedPhotoId();
                }
                //当在列表没有选择图片，又在详情界面时默认选择当前图片
                if (selectedPhotosid.size() <= 0) {
                    if (imagePagerFragment != null && imagePagerFragment.isResumed()) {
                        // 预览界面
                        selectedPhotosid = imagePagerFragment.getCurrentID();
                    }
                }
                if (selectedPhotosid != null && selectedPhotosid.size() > 0) {
                    intent.putIntegerArrayListExtra(KEY_SELECTED_PHOTOS_ID, selectedPhotosid);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            } else {
                ArrayList<String> selectedPhotos = null;
                if (pickerFragment != null) {
                    selectedPhotos = pickerFragment.getPhotoGridAdapter().getSelectedPhotoPaths();
                }
                //当在列表没有选择图片，又在详情界面时默认选择当前图片
                if (selectedPhotos.size() <= 0) {
                    if (imagePagerFragment != null && imagePagerFragment.isResumed()) {
                        // 预览界面
                        selectedPhotos = imagePagerFragment.getCurrentPath();
                    }
                }
                if (selectedPhotos != null && selectedPhotos.size() > 0) {
                    intent.putStringArrayListExtra(KEY_SELECTED_PHOTOS_PATH, selectedPhotos);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public PhotoPickerActivity getActivity() {
        return this;
    }

    public boolean isShowGif() {
        return showGif;
    }

    public void setShowGif(boolean showGif) {
        this.showGif = showGif;
    }
}
