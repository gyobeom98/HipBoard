package com.gyobeom29.hipboard.adapter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gyobeom29.hipboard.PostInfo;
import com.gyobeom29.hipboard.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainPostAdapter extends RecyclerView.Adapter<MainPostAdapter.GalleryViewHolder> {
 private static final String TAG = "MainPostAdapter";

private ArrayList<PostInfo> mDataset;
private Activity activity;
// Provide a reference to the views for each data item
// Complex data items may need more than one view per item, and
// you provide access to all the views for a data item in a view holder
public static class GalleryViewHolder extends RecyclerView.ViewHolder {
    // each data item is just a string in this case
    public CardView cardView;
    public GalleryViewHolder(CardView v) {
        super(v);
        cardView = v;
    }
}

    // Provide a suitable constructor (depends on the kind of dataset)
    public MainPostAdapter(ArrayList<PostInfo> myDataset, Activity activity) {
        mDataset = myDataset;
        this.activity = activity;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MainPostAdapter.GalleryViewHolder onCreateViewHolder(ViewGroup parent,
                                                                int viewType) {
        // create a new view
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        final GalleryViewHolder galleryViewHolder = new GalleryViewHolder(cardView);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.putExtra("profilePath",mDataset.get(galleryViewHolder.getAdapterPosition()));
//                activity.setResult(Activity.RESULT_OK,intent);
//                activity.finish();
            }
        });

        return galleryViewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final GalleryViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        CardView cardView = holder.cardView;
        TextView titleTextView = cardView.findViewById(R.id.item_post_textView);
        titleTextView.setText(mDataset.get(position).getTitle());

        TextView createAtTextview = cardView.findViewById(R.id.post_date_textView);
        createAtTextview.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(mDataset.get(position).getCreateAt()));

        Log.i(TAG,mDataset.get(position).toString());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}