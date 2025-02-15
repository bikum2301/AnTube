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
        Log.d("MainActivity", "onCreate: MainActivity bắt đầu"); // Log 1: Bắt đầu onCreate
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Log.d("MainActivity", "onCreate: setContentView đã được gọi"); // Log 2: Sau setContentView

        player = new ExoPlayer.Builder(this).build();
        playerView = findViewById(R.id.player_view);
        playerView.setPlayer(player);
        Log.d("MainActivity", "onCreate: player và playerView đã được khởi tạo"); // Log 3: Sau player và playerView

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        Log.d("MainActivity", "onCreate: bottomNavigationView đã được khởi tạo"); // Log 4: Sau bottomNavigationView

        chooseVideoButton = findViewById(R.id.choose_video_button);
        Log.d("MainActivity", "onCreate: chooseVideoButton đã được khởi tạo"); // Log 5: Sau chooseVideoButton

        sessionManager = new SessionManager(this);
        Log.d("MainActivity", "onCreate: sessionManager đã được khởi tạo"); // Log 6: Sau sessionManager

        Log.d("MainActivity", "onCreate: Kiểm tra đăng nhập..."); // Log 7: Trước kiểm tra đăng nhập
        if (!sessionManager.isLoggedIn()) {
            Log.d("MainActivity", "onCreate: Chưa đăng nhập, chuyển hướng đến LoginActivity"); // Log 8: Chưa đăng nhập
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            Log.d("MainActivity", "onCreate: finish() đã được gọi vì chưa đăng nhập"); // Log 9: Sau finish() vì chưa đăng nhập
            return; // Dừng khởi tạo MainActivity nếu chưa đăng nhập
        }
        Log.d("MainActivity", "onCreate: Đã đăng nhập"); // Log 10: Đã đăng nhập

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
        Log.d("MainActivity", "onCreate: pickMediaLauncher đã được khởi tạo"); // Log 11: Sau pickMediaLauncher

        // Loại bỏ loadLocalVideos() và RecyclerView/Adapter setup ở đây

        chooseVideoButton.setOnClickListener(new View.OnClickListener() { // Set OnClickListener cho Button
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "chooseVideoButton: onClick được gọi"); // Log 12: Button onClick
                launchPhotoPicker(); // Gọi launchPhotoPicker khi button được click
            }
        });
        Log.d("MainActivity", "onCreate: setOnClickListener cho chooseVideoButton đã được gọi"); // Log 13: Sau setOnClickListener cho button


        player.setPlayWhenReady(true);
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                Log.d("MainActivity", "onPlaybackStateChanged: state = " + state); // Log 14: onPlaybackStateChanged
                if (state == Player.STATE_READY) {
                    Log.d("MainActivity", "onPlaybackStateChanged: STATE_READY, gọi startForegroundService"); // Log 15: STATE_READY
                    startForegroundService();
                }
            }
        });
        Log.d("MainActivity", "onCreate: addListener cho player đã được gọi"); // Log 16: Sau addListener cho player
        Log.d("MainActivity", "onCreate: MainActivity onCreate hoàn thành"); // Log 17: Kết thúc onCreate
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("MainActivity", "onStart: MainActivity onStart được gọi"); // Log 18: onStart
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "onResume: MainActivity onResume được gọi"); // Log 19: onResume
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MainActivity", "onPause: MainActivity onPause được gọi"); // Log 20: onPause
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("MainActivity", "onStop: MainActivity onStop được gọi"); // Log 21: onStop
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("MainActivity", "onCreateOptionsMenu: onCreateOptionsMenu được gọi"); // Log 22: onCreateOptionsMenu
        getMenuInflater().inflate(R.menu.main_menu, menu); // Tạo menu từ main_menu.xml
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("MainActivity", "onOptionsItemSelected: onOptionsItemSelected được gọi, item id = " + item.getItemId()); // Log 23: onOptionsItemSelected
        int id = item.getItemId();
        if (id == R.id.action_profile) {
            Log.d("MainActivity", "onOptionsItemSelected: action_profile được chọn"); // Log 24: action_profile
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            Log.d("MainActivity", "onOptionsItemSelected: action_logout được chọn"); // Log 25: action_logout
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        Log.d("MainActivity", "logout: logout được gọi"); // Log 26: logout
        sessionManager.clearSession();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finishAffinity(); // Đóng tất cả các activity
        Log.d("MainActivity", "logout: Đã clear session, chuyển hướng LoginActivity và finishAffinity"); // Log 27: Sau logout actions
    }


    private void startForegroundService() {
        Log.d("MainActivity", "startForegroundService: startForegroundService được gọi"); // Log 28: startForegroundService
        Intent intent = new Intent(this, PlaybackService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        Log.d("MainActivity", "startForegroundService: Đã gọi startForegroundService/startService"); // Log 29: Sau startForegroundService/startService
    }

    @Override
    public void onUserLeaveHint() {
        super.onUserLeaveHint();
        Log.d("MainActivity", "onUserLeaveHint: onUserLeaveHint được gọi"); // Log 30: onUserLeaveHint
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PictureInPictureParams params = new PictureInPictureParams.Builder().build();
            enterPictureInPictureMode(params);
            Log.d("MainActivity", "onUserLeaveHint: enterPictureInPictureMode được gọi"); // Log 31: Sau enterPictureInPictureMode
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MainActivity", "onDestroy: onDestroy được gọi"); // Log 32: onDestroy
        player.release();
        Log.d("MainActivity", "onDestroy: player.release() đã được gọi"); // Log 33: Sau player.release()
    }


    private void launchPhotoPicker() {
        Log.d("MainActivity", "launchPhotoPicker: launchPhotoPicker được gọi"); // Log 34: launchPhotoPicker
        pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE)
                .build());
        Log.d("MainActivity", "launchPhotoPicker: pickMediaLauncher.launch() đã được gọi"); // Log 35: Sau pickMediaLauncher.launch()
    }


    private void playVideo(String videoUrl) {
        Log.d("MainActivity", "playVideo: playVideo được gọi, videoUrl = " + videoUrl); // Log 36: playVideo
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
        Log.d("MainActivity", "playVideo: setMediaItem, prepare, play đã được gọi"); // Log 37: Sau playVideo actions
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("MainActivity", "onConfigurationChanged: onConfigurationChanged được gọi, newConfig.orientation = " + newConfig.orientation); // Log 38: onConfigurationChanged
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d("MainActivity", "onConfigurationChanged: Chế độ landscape"); // Log 39: Landscape
            hideSystemUI();
            playerView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            playerView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            chooseVideoButton.setVisibility(View.GONE); // Ẩn button ở chế độ landscape
            bottomNavigationView.setVisibility(View.GONE);
            Log.d("MainActivity", "onConfigurationChanged: Đã cập nhật layout cho landscape"); // Log 40: Sau layout landscape
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d("MainActivity", "onConfigurationChanged: Chế độ portrait"); // Log 41: Portrait
            showSystemUI();
            playerView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            playerView.getLayoutParams().height = (int) getResources().getDimension(R.dimen.player_view_height_portrait);
            chooseVideoButton.setVisibility(View.VISIBLE); // Hiện button ở chế độ portrait
            bottomNavigationView.setVisibility(View.VISIBLE);
            Log.d("MainActivity", "onConfigurationChanged: Đã cập nhật layout cho portrait"); // Log 42: Sau layout portrait
        }
        playerView.requestLayout();
        Log.d("MainActivity", "onConfigurationChanged: playerView.requestLayout() đã được gọi"); // Log 43: Sau requestLayout
    }

    private void hideSystemUI() {
        Log.d("MainActivity", "hideSystemUI: hideSystemUI được gọi"); // Log 44: hideSystemUI
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        Log.d("MainActivity", "hideSystemUI: setSystemUiVisibility đã được gọi"); // Log 45: Sau setSystemUiVisibility hide
    }

    private void showSystemUI() {
        Log.d("MainActivity", "showSystemUI: showSystemUI được gọi"); // Log 46: showSystemUI
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        Log.d("MainActivity", "showSystemUI: setSystemUiVisibility đã được gọi"); // Log 47: Sau setSystemUiVisibility show
    }
}