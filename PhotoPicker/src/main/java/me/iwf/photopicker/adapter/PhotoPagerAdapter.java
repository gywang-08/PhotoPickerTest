package me.iwf.photopicker.adapter;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;

import android.os.Build;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.R;
import me.iwf.photopicker.utils.AndroidLifecycleUtils;

/**
 * Created by donglua on 15/6/21.
 */
public class PhotoPagerAdapter extends PagerAdapter {

    private List<String> paths = new ArrayList<>();
    private List<Integer> ids = new ArrayList<>();
    private RequestManager mGlide;

    public PhotoPagerAdapter(RequestManager glide, List<String> paths,List<Integer>ids) {
        this.paths = paths;
        this.mGlide = glide;
        this.ids = ids;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final Context context = container.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.__picker_picker_item_pager, container, false);

        final ImageView imageView = (ImageView) itemView.findViewById(R.id.iv_pager);

        final Uri uri;
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            final int id = ids.get(position);
            uri = MediaStore.Images.Media
                    .EXTERNAL_CONTENT_URI
                    .buildUpon()
                    .appendPath(String.valueOf(id)).build();
        }else {
            final String path = paths.get(position);

            if (path.startsWith("http")) {
                uri = Uri.parse(path);
            } else {
                uri = Uri.fromFile(new File(path));
            }
        }

        boolean canLoadImage = AndroidLifecycleUtils.canLoadImage(context);

        if (canLoadImage) {
            final RequestOptions options = new RequestOptions();
            options.dontAnimate()
                    .dontTransform()
                    .override(800, 800)
                    .placeholder(R.drawable.__picker_ic_photo_black_48dp)
                    .error(R.drawable.__picker_ic_broken_image_black_48dp);
            mGlide.setDefaultRequestOptions(options).load(uri)
                    .thumbnail(0.1f)
                    .into(imageView);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (context instanceof Activity) {
                    if (!((Activity) context).isFinishing()) {
                        ((Activity) context).onBackPressed();
                    }
                }
            }
        });

        container.addView(itemView);

        return itemView;
    }


    @Override
    public int getCount() {
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            return ids.size();
        } else {
            return paths.size();
        }
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        mGlide.clear((View) object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

}
