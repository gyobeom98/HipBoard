package com.gyobeom29.hipboard.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.gyobeom29.hipboard.R;

import java.util.ArrayList;

public class YoutubeViewPageAdapter extends PagerAdapter {

    private ArrayList<String> imagePathList;

    private LayoutInflater layoutInflater;

    private Context context;

    public YoutubeViewPageAdapter(Context context,ArrayList<String> imagePathList) {
        this.context = context;
        this.imagePathList = imagePathList;
    }

    @Override
    public int getCount() {
        return imagePathList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == ((View)object);
    }


    public View instantiateItem(ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = layoutInflater.inflate(R.layout.youtube_image_slider, container, false);
        ImageView imageView = (ImageView)v.findViewById(R.id.youtubeViewPageInnerImageView);
        Glide.with(context).load(imagePathList.get(position)).centerCrop().into(imageView);
        container.addView(v);
        Log.i("YoutubeViewPageAdapter",imagePathList.get(position));
        String getPath = imagePathList.get(position);
        String videoId = getPath.substring(getPath.indexOf("/",getPath.indexOf("vi/"))+1,getPath.lastIndexOf("/"));
        final String goPath = "https://www.youtube.com/watch?v="+videoId;
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(goPath)).setPackage("com.google.android.youtube");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                Log.i("YoutubeViewPageAdapter","누름 : " + goPath);
            }
        });

        return v;
    }

    //할당을 해제
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.invalidate();
//        super.destroyItem(container, position, object);
    }


}
