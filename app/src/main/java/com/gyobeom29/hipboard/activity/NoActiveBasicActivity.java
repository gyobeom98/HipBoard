package com.gyobeom29.hipboard.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.gyobeom29.hipboard.R;

public class NoActiveBasicActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
    }

    public void setActionBarTitle(String title){
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(title);
        }
    }

    public void setActionBarTitle(@StringRes int stringRes){
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(stringRes);
        }
    }

    public void showHome(){
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home : finish(); break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addToolbarItem(View v, View.OnClickListener onClickListener){
        LinearLayout toolbarlayout = findViewById(R.id.toolbar_menu_lay);
        toolbarlayout.addView(v);
        v.setOnClickListener(onClickListener);
    }


}
