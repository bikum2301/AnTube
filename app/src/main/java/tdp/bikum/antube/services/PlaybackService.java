package tdp.bikum.antube.services;

import android.app.*;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.ui.PlayerNotificationManager;

import tdp.bikum.antube.MainActivity;
import tdp.bikum.antube.R;

@UnstableApi
public class PlaybackService extends Service {
    private ExoPlayer player;
    private MediaSession mediaSession;
    private PlayerNotificationManager playerNotificationManager;
    private static final String CHANNEL_ID = "playback_channel";
    private static final int NOTIFICATION_ID = 1;
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String VIDEO_URL_EXTRA = "VIDEO_URL_EXTRA";


    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        player = new ExoPlayer.Builder(this).build();
        mediaSession = new MediaSession.Builder(this, player).build();

        // Khởi tạo PlayerNotificationManager
        playerNotificationManager = new PlayerNotificationManager.Builder(
                this,
                NOTIFICATION_ID,
                CHANNEL_ID)
                .setMediaDescriptionAdapter(new DescriptionAdapter())
                .setChannelNameResourceId(R.string.playback_channel_name)
                .setChannelDescriptionResourceId(R.string.playback_channel_description)
                .build();
        playerNotificationManager.setPlayer(player);
        playerNotificationManager.setMediaSessionToken(mediaSession.getSessionCompatToken()); // Set MediaSession token
        playerNotificationManager.setSmallIcon(R.drawable.ic_notification); // Icon nhỏ cho notification
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.playback_channel_name), // Sử dụng string resource
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(getString(R.string.playback_channel_description)); // Sử dụng string resource
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        String videoUrl = intent.getStringExtra(VIDEO_URL_EXTRA);

        if (videoUrl != null) {
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
        }


        if (ACTION_PLAY.equals(action)) {
            player.play();
        } else if (ACTION_PAUSE.equals(action)) {
            player.pause();
        } else if (ACTION_STOP.equals(action)) {
            playerNotificationManager.setPlayer(null); // Loại bỏ notification khi stop
            stopForeground(Service.STOP_FOREGROUND_REMOVE);
            stopSelf();
        } else {
            startForeground(NOTIFICATION_ID, new NotificationCompat.Builder(this, CHANNEL_ID).build()); // Cần startForeground để service chạy nền
        }


        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        playerNotificationManager.setPlayer(null); // Loại bỏ notification khi service destroy
        player.release();
        mediaSession.release();
    }

    // Implement DescriptionAdapter để cung cấp metadata cho notification
    private class DescriptionAdapter implements PlayerNotificationManager.MediaDescriptionAdapter {

        @Override
        public CharSequence getCurrentContentTitle(Player player) {
            // Lấy title video hiện tại từ player hoặc nguồn dữ liệu của bạn
            // Ví dụ:
            // MediaItem mediaItem = player.getCurrentMediaItem();
            // if (mediaItem != null) {
            //     return mediaItem.mediaMetadata.title != null ? mediaItem.mediaMetadata.title : "Unknown Title";
            // }
            return "Now Playing"; // Placeholder title
        }

        @Nullable
        @Override
        public CharSequence getCurrentContentText(Player player) {
            // Lấy subtitle hoặc thông tin bổ sung (ví dụ: artist)
            return "Background Playback"; // Placeholder text
        }

        @Nullable
        @Override
        public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
            // Load large icon (ví dụ: album art hoặc video thumbnail)
            // Bạn có thể load từ URL hoặc local resource và gọi callback.onBitmap() khi load xong
            return null; // Placeholder - bạn có thể load icon và trả về Bitmap ở đây
        }

        @Nullable
        @Override
        public PendingIntent createCurrentContentIntent(Player player) {
            // Intent khi người dùng click vào notification
            Intent openAppIntent = new Intent(PlaybackService.this, MainActivity.class);
            return PendingIntent.getActivity(PlaybackService.this, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE);
        }
    }
}