package truongan.nguyen.n01639800.lab12;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<Course> courseList;
    private OnItemLongClickListener longClickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public CourseAdapter(List<Course> courses) {
        this.courseList = courses;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new CourseViewHolder(itemView, longClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.tvName.setText(course.getName());
        holder.tvDescription.setText(course.getDescription());
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public void setCourseList(List<Course> courses) {
        this.courseList = courses;
        notifyDataSetChanged();
    }

    public Course getCourseAt(int position) {
        return courseList.get(position);
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvDescription;

        public CourseViewHolder(@NonNull View itemView, final OnItemLongClickListener longClickListener) {
            super(itemView);
            tvName = itemView.findViewById(android.R.id.text1);
            tvDescription = itemView.findViewById(android.R.id.text2);

            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        longClickListener.onItemLongClick(pos);
                        return true;
                    }
                }
                return false;
            });
        }
    }
}

