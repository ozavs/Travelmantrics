package com.example.travelmantrics;

import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DealAdapter extends RecyclerView.Adapter<DealAdapter.DealViewHolder> {

    ArrayList<TravelDeal> deals;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private ImageView imageDeal;

    public DealAdapter() {
        mDatabase = FirebaseUtils.mDatabase;
        mReference = FirebaseUtils.mDatabaseReference;
        deals = FirebaseUtils.mDeals;

        mReference.addChildEventListener ( new ChildEventListener ( ) {
            @Override
            public void onChildAdded ( @NonNull DataSnapshot dataSnapshot , @Nullable String s ) {
                TravelDeal tv = dataSnapshot.getValue (TravelDeal.class);
                tv.setId ( dataSnapshot.getKey ());
                deals.add (tv);

                notifyItemInserted ( deals.size () - 1 );
            }

            @Override
            public void onChildChanged ( @NonNull DataSnapshot dataSnapshot , @Nullable String s ) {

            }

            @Override
            public void onChildRemoved ( @NonNull DataSnapshot dataSnapshot ) {

            }

            @Override
            public void onChildMoved ( @NonNull DataSnapshot dataSnapshot , @Nullable String s ) {

            }

            @Override
            public void onCancelled ( @NonNull DatabaseError databaseError ) {

            }
        } );
    }

    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder ( @NonNull ViewGroup parent , int viewType ) {

        View view = LayoutInflater.from (parent.getContext())
                .inflate ( R.layout.rv_row, parent, false );

        return new DealViewHolder (view);
    }

    @Override
    public void onBindViewHolder ( @NonNull DealViewHolder holder , int position ) {
        TravelDeal tv = deals.get(position);
        holder.bind (tv);
    }

    @Override
    public int getItemCount () {
        return deals.size();
    }

    public class DealViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvTitle, tvPrice, tvDescription;

        public DealViewHolder(View itemView) {

            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            imageDeal = itemView.findViewById(R.id.imageDeal);

            itemView.setOnClickListener(this);

        }

        public void bind(TravelDeal deal) {
            tvTitle.setText(deal.getTitle());
            tvPrice.setText(deal.getPrice());
            tvDescription.setText(deal.getDescription());
            showImage(deal.getImageUrl());
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            TravelDeal selectedDeal = deals.get(position);
            Intent intent = new Intent(view.getContext(), DealActivity.class);
            intent.putExtra("deal", selectedDeal);

            view.getContext().startActivity(intent);
        }


        private void showImage(String url) {

            if(url != null && !url.isEmpty()) {

                //get the screen width
                int width = Resources.getSystem().getDisplayMetrics().widthPixels;
                Picasso.with(imageDeal.getContext())
                        .load(url)
                        .placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .resize(120, 120)
                        .centerCrop()
                        .into(imageDeal);
            }
        }

    }
}
