package com.cuonghx.teacher.teachercheckin.screens.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cuonghx.teacher.teachercheckin.R;
import com.cuonghx.teacher.teachercheckin.data.model.Student;
import com.cuonghx.teacher.teachercheckin.screens.BaseRecyclerViewAdapter;
import com.cuonghx.teacher.teachercheckin.screens.home.ViewActivity;

import androidx.recyclerview.widget.RecyclerView;

public class ListAdapter extends BaseRecyclerViewAdapter<Student, ListAdapter.ViewHolder> {

    private int courseId;

    public ListAdapter(Context context, int courseId) {
        super(context);
        this.courseId = courseId;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
//        // Inflate the custom layout
        View  courseView = layoutInflater.inflate(R.layout.item_rcview_list, parent, false);
        return new ViewHolder(courseView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.setupView(mCollection.get(position), mContext);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTextViewName;
        private TextView mTexViewAttend;
        private Context mContext;
        private Student curentStudent;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextViewName = itemView.findViewById(R.id.tv_it_student_name);
            mTexViewAttend = itemView.findViewById(R.id.tv_it_attend_num);
            itemView.findViewById(R.id.bt_view).setOnClickListener(this);
        }
        public void setupView(Student student, Context context){
            this.mContext = context;
            curentStudent = student;
            mTextViewName.setText(student.getmName());
            mTexViewAttend.setText(String.valueOf(student.getmCount()));
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.bt_view:
                    Intent intent = new Intent(mContext, ViewActivity.class);
                    intent.putExtra("student_id", curentStudent.getmId());
                    intent.putExtra("course_id", courseId);
//                    intent.putExtra("course_name", mCurrentCourse.getName());
                    mContext.startActivity(intent);
                    break;
            }
        }
    }
}
