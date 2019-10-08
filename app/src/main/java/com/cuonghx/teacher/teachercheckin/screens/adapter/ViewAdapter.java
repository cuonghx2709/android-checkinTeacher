package com.cuonghx.teacher.teachercheckin.screens.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cuonghx.teacher.teachercheckin.R;
import com.cuonghx.teacher.teachercheckin.screens.BaseRecyclerViewAdapter;
import com.squareup.picasso.Picasso;

import androidx.recyclerview.widget.RecyclerView;

public class ViewAdapter extends BaseRecyclerViewAdapter<String, ViewAdapter.ViewHolder> {
    public ViewAdapter(Context context) {
        super(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
//        // Inflate the custom layout
        View  imageView = layoutInflater.inflate(R.layout.item_rcview_view, parent, false);

        return new ViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.setupView(mCollection.get(position));
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_item_view);
        }
        private void setupView(String url){
            Picasso.get().load(url).into(imageView);
        }
    }
}
