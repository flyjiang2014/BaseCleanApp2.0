package com.flyjiang.cleanapp.widget;

import android.content.Context;
import android.graphics.Outline;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.youth.banner.loader.ImageLoaderInterface;

/**
 * Created by  on 2019/11/19.
 * 文件说明：
 */
public class GlideRoundImageLoader implements ImageLoaderInterface<ImageView> {
    @Override
    public ImageView createImageView(Context context) {
        return new ImageView(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void displayImage(Context context, Object path, ImageView imageView) {
        //Glide 加载图片简单用法
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            imageView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(20, 20, view.getWidth() - 20, view.getHeight(), 30);
                }
            });
        }
        imageView.setClipToOutline(true);
        Glide.with(context).load(path).into(imageView);
    }
}
