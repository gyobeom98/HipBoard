package com.gyobeom29.hipboard.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.gyobeom29.hipboard.R;
import com.gyobeom29.hipboard.adapter.GalleryAdapter;

import java.util.ArrayList;

public class GalleryActivity extends BasicActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        recyclerView.setHasFixedSize(true);

        final int numberOfColumns = 3;

        // use a linear layout manager
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(),numberOfColumns));

        mAdapter = new GalleryAdapter(getImagesPath(this),this);
        recyclerView.setAdapter(mAdapter);
    }


    public  ArrayList<String> getImagesPath(Activity activity){
        Uri uri= null;
        ArrayList<String> listOfAllImages = new ArrayList<>();
        Cursor cursor;
        int column_index_data;
        String pathofImage = null;
        Intent intent = getIntent();
        String[] projection = null;
        if(intent !=null){
            if(intent.getStringExtra("media").equals("video")){
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                projection = new String[]{MediaStore.MediaColumns.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.DATE_ADDED};
            }else{
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                projection = new String[]{MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATE_ADDED};
            }
        }
        cursor = activity.getContentResolver().query(uri,projection,null,null,MediaStore.Images.Media.DATE_ADDED+" desc");
        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

        while (cursor.moveToNext()){
            pathofImage = cursor.getString(column_index_data);
            listOfAllImages.add(pathofImage);
        }
        return listOfAllImages;
    }

}
