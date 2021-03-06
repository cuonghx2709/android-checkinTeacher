package com.cuonghx.teacher.teachercheckin.screens.adapter;

import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cuonghx.teacher.teachercheckin.R;
import com.cuonghx.teacher.teachercheckin.data.model.ChatLog;
import com.cuonghx.teacher.teachercheckin.screens.BaseRecyclerViewAdapter;
import com.cuonghx.teacher.teachercheckin.util.CircleTransform;
import com.squareup.picasso.Picasso;

import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.recyclerview.widget.RecyclerView;
import retrofit2.HttpException;

public class ChatLogAdapter extends BaseRecyclerViewAdapter<ChatLog, ChatLogAdapter.ViewHolder> {

    private int viewtype;

    public ChatLogAdapter(Context context) {
        super(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        // Inflate the custom layout
        View courseView;
        Log.d("cuonghx", "onCreateViewHolder: " + viewType);
        switch (viewType){
            case 1 :
                courseView = layoutInflater.inflate(R.layout.item_rv_chat, parent, false);
                break;
            case 2:
                courseView = layoutInflater.inflate(R.layout.item_rv_reply, parent, false);
                break;
            default:
                courseView = null;
                break;
        }

        // Return a new holder instance
        ChatLogAdapter.ViewHolder viewHolder = new ChatLogAdapter.ViewHolder(courseView);
        return viewHolder;
    }

    @Override
    public void addItem(ChatLog item) {
        super.addItem(item);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        ChatLog chatLog = mCollection.get(position);
        holder.updateView(chatLog);
    }

    @Override
    public int getItemViewType(int position) {
        ChatLog chatLog = mCollection.get(position);
        Log.d("cuonghx", "getItemViewType: " + chatLog.isTeacher);
        if (chatLog.isTeacher){
            viewtype = 1;
            return 1;
        }else  {
            viewtype = 2;
            return 2;
        }
    }

    public class ViewHolder  extends RecyclerView.ViewHolder {

        private TextView textView;
        private TextView tvDate;
        private TextView tvName;

        public ViewHolder(final View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv_text_chat);
//            imageViewReply = itemView.findViewById(R.id.iv_avatar_reply);
//            imageViewChat = itemView.findViewById(R.id.iv_avatar_chat);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvName = itemView.findViewById(R.id.tv_name);
            itemView.isClickable();
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    ClipboardManager cm = (ClipboardManager)itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setText(textView.getText());
                    Toast.makeText(itemView.getContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
        public void updateView(ChatLog chatLog){
            if (!chatLog.isTeacher){
                tvName.setText(chatLog.name);
                textView.setText(chatLog.text);
                Date d = new Date(chatLog.timestamp);
                DateFormat f = new SimpleDateFormat("HH:mm, dd/MM");
                tvDate.setText(f.format(d));
//                Picasso.get().load("https://wallpapertag.com/wallpaper/full/e/6/d/149828-fairy-tail-logo-wallpaper-1920x1200-pictures.jpg").transform(new CircleTransform()).into(imageViewReply);
            }else {
                Date d = new Date(chatLog.timestamp);
                DateFormat f = new SimpleDateFormat("HH:mm, dd/MM");
                tvDate.setText(f.format(d));
                textView.setText(chatLog.text);
//                Picasso.get().load("https://wallpapertag.com/wallpaper/full/e/6/d/149828-fairy-tail-logo-wallpaper-1920x1200-pictures.jpg").transform(new CircleTransform()).into(imageViewChat);
            }
        }

        private void handleErrors(Throwable throwable) {
            if (throwable instanceof HttpException) {
                handleHttpExceptions((HttpException) throwable);
                return;
            } else if (throwable instanceof UnknownHostException) {
                Toast.makeText(this.itemView.getContext(), R.string.msg_check_internet_connection, Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this.itemView.getContext(),
                    R.string.msg_something_went_wrong,
                    Toast.LENGTH_SHORT)
                    .show();
        }

        private void handleHttpExceptions(HttpException httpException) {
            switch (httpException.code()) {
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    Toast.makeText(this.itemView.getContext(),
                            R.string.msg_wrong_email_or_password,
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(this.itemView.getContext(), httpException.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
