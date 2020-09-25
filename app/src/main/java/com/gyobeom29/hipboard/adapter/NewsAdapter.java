package com.gyobeom29.hipboard.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.gyobeom29.hipboard.NewsArticle;
import com.gyobeom29.hipboard.R;
import com.gyobeom29.hipboard.activity.DetailNewsActivity;

import java.util.ArrayList;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    ArrayList<NewsArticle> myNewsItems;
    Context context;

    public NewsAdapter(ArrayList<NewsArticle> myNewsItems, Context context) {
        this.myNewsItems = myNewsItems;
        this.context = context;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.news_item_layout,parent,false);
        return new NewsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, final int position) {
        GridLayout layout = holder.newsItemLayout;

        ((TextView)layout.findViewById(R.id.news_item_title_text_view)).setText(myNewsItems.get(position).getTitle());
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailNewsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("link",myNewsItems.get(position).getLink());
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() { return myNewsItems.size(); }

    public static class NewsViewHolder extends RecyclerView.ViewHolder{
        public GridLayout newsItemLayout;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            newsItemLayout = (GridLayout)itemView;
        }
    }

}
