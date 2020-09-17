package com.gyobeom29.hipboard.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.gyobeom29.hipboard.R;
import com.gyobeom29.hipboard.adapter.GalleryAdapter;

import java.util.ArrayList;

public class GalleryActivity extends NoActiveBasicActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        recyclerView = findViewById(R.id.recyclerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            if(ActivityCompat.shouldShowRequestPermissionRationale(GalleryActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                ActivityCompat.requestPermissions(GalleryActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

            }else{
                ActivityCompat.requestPermissions(GalleryActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                startingToast("권한을 허용 해주세요");
            }
        }
        setActionBarTitle("갤러리");
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

    public void startingToast(String msg){
        Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT).show();
    }

    private void startActi(Class c){
        Intent intent = new Intent(GalleryActivity.this,c);
        startActivity(intent);
    }

}
