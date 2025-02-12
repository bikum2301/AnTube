package tdp.bikum.antube;

import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import tdp.bikum.antube.services.PlaybackService;
import android.view.Menu;
import android.view.MenuItem;

import tdp.bikum.antube.utils.SessionManager;

@UnstableApi
public class MainActivity extends AppCompatActivity { // Loại bỏ implements VideoAdapter.OnVideoClickListener

    private ExoPlayer player;
    private PlayerView playerView;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;
    private com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigationView;
    private Button chooseVideoButton;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        player = new ExoPlayer.Builder(this).build();
        playerView = findViewById(R.id.player_view);
        playerView.setPlayer(player);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        chooseVideoButton = findViewById(R.id.choose_video_button);
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return; // Dừng khởi tạo MainActivity nếu chưa đăng nhập
        }

        // Khởi tạo ActivityResultLauncher cho Photo Picker
        pickMediaLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: " + uri);
                playVideo(uri.toString());
            } else {
                Log.d("PhotoPicker", "No media selected");
                Toast.makeText(this, "Không có video nào được chọn", Toast.LENGTH_SHORT).show();
            }
        });

        // Loại bỏ loadLocalVideos() và RecyclerView/Adapter setup ở đây

        chooseVideoButton.setOnClickListener(new View.OnClickListener() { // Set OnClickListener cho Button
            @Override
            public void onClick(View v) {
                launchPhotoPicker(); // Gọi launchPhotoPicker khi button được click
            }
        });


        player.setPlayWhenReady(true);
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    startForegroundService();
                }
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu); // Tạo menu từ main_menu.xml
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_profile) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        sessionManager.clearSession();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finishAffinity(); // Đóng tất cả các activity
    }


    private void startForegroundService() {
        Intent intent = new Intent(this, PlaybackService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @Override
    public void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PictureInPictureParams params = new PictureInPictureParams.Builder().build();
            enterPictureInPictureMode(params);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }


    private void launchPhotoPicker() {
        pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE)
                .build());
    }


    private void playVideo(String videoUrl) {
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUI();
            playerView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            playerView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            chooseVideoButton.setVisibility(View.GONE); // Ẩn button ở chế độ landscape
            bottomNavigationView.setVisibility(View.GONE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            showSystemUI();
            playerView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            playerView.getLayoutParams().height = (int) getResources().getDimension(R.dimen.player_view_height_portrait);
            chooseVideoButton.setVisibility(View.VISIBLE); // Hiện button ở chế độ portrait
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
        playerView.requestLayout();
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }
}