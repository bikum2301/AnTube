package tdp.bikum.antube.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import tdp.bikum.antube.R;
import tdp.bikum.antube.models.Video;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder>{
    private Context context; // Thêm Context
    private List<Video> videos;
    private OnVideoClickListener listener;

    public VideoAdapter(List<Video> videos, OnVideoClickListener listener) {
        this.videos = videos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext(); // Khởi tạo Context ở đây
        View view = LayoutInflater.from(context) // Sử dụng context đã khởi tạo
                .inflate(R.layout.video_item, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Video video = videos.get(position);
        holder.titleTextView.setText(video.getTitle());

        // Load thumbnail bằng Glide (nếu có thumbnailUrl)
        if (video.getThumbnailUrl() != null && !video.getThumbnailUrl().isEmpty()) {
            Glide.with(context) // Sử dụng context đã khởi tạo
                    .load(Uri.parse(video.getThumbnailUrl()))
                    .placeholder(R.drawable.ic_video_thumbnail_placeholder)
                    .error(R.drawable.ic_video_thumbnail_placeholder)
                    .into(holder.thumbnailImageView);
        } else {
            holder.thumbnailImageView.setImageResource(R.drawable.ic_video_thumbnail_placeholder);
        }


        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVideoClick(video);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ImageView thumbnailImageView;

        VideoViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.video_title);
            thumbnailImageView = itemView.findViewById(R.id.video_thumbnail);
        }
    }

    public interface OnVideoClickListener {
        void onVideoClick(Video video);
    }
}