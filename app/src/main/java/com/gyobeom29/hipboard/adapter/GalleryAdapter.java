package com.gyobeom29.hipboard.adapter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gyobeom29.hipboard.R;
import com.gyobeom29.hipboard.activity.GalleryActivity;
import com.gyobeom29.hipboard.activity.MemberInitActivity;

import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {
private ArrayList<String> mDataset;
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
    public GalleryAdapter(ArrayList<String> myDataset, Activity activity) {
        mDataset = myDataset;
        this.activity = activity;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public GalleryAdapter.GalleryViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gallery, parent, false);
        final GalleryViewHolder galleryViewHolder = new GalleryViewHolder(cardView);

        return galleryViewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final GalleryViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        CardView cardView = holder.cardView;
        ImageView imageView = cardView.findViewById(R.id.imageView);

        String profilePath = mDataset.get(position);
        Log.e("로그 : ","profilePath : " + profilePath);

        Glide.with(activity).load(profilePath).centerCrop().override(500).into(imageView);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Log.i("GalleryAdapter","갤러리 아답터 까지 옴");
                Log.i("GalleryAdapter","갤러리 아이템 : " + mDataset.get(position));
                intent.putExtra("profilePath",mDataset.get(position));
                Log.i("GalleryAdapter","activityName : " + activity.getClass().getName());
                activity.setResult(Activity.RESULT_OK,intent);

                activity.finish();

            }
        });


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
