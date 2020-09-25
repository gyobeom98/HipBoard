package com.gyobeom29.hipboard.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gyobeom29.hipboard.PostInfo;
import com.gyobeom29.hipboard.R;
import com.gyobeom29.hipboard.activity.DetailPostActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MyPostAdapter extends RecyclerView.Adapter<MyPostAdapter.MyPostAdapterViewHolder> {

    ArrayList<PostInfo> myPostItems;
    Context context;

    public MyPostAdapter(ArrayList<PostInfo> myPostItems,Context context){
        this.myPostItems = myPostItems;
        this.context = context;
    }

    @NonNull
    @Override
    public MyPostAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View itemView = inflater.inflate(R.layout.my_post_item,parent,false);

        return new MyPostAdapterViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyPostAdapterViewHolder holder, final int position) {
        LinearLayout layout = holder.myPostItemLayout;
        TextView titleTextView = layout.findViewById(R.id.myPostInnerTitleTextView);
        titleTextView.setText("["+myPostItems.get(position).getTitle()+"]");
        TextView contentTextView = layout.findViewById(R.id.myPostInnerContentTextView);
        contentTextView.setText(myPostItems.get(position).getContents().get(0));
        TextView publisherNameTextView = layout.findViewById(R.id.myPostInnerPublisherNameTextView);
        publisherNameTextView.setText(myPostItems.get(position).getPublisherName());
        TextView createAtTextView = layout.findViewById(R.id.myPostInnerCreateAtTextView);
        createAtTextView.setText(new SimpleDateFormat("yy-MM-dd HH:mm:ss", Locale.getDefault()).format(myPostItems.get(position).getCreateAt()));

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailPostActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("documentId",myPostItems.get(position).getDocumentId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return myPostItems.size();
    }

    public static class MyPostAdapterViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout myPostItemLayout;

        public MyPostAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            myPostItemLayout = (LinearLayout) itemView;

        }

    }

}
