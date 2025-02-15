package tdp.bikum.antube;

import android.Manifest;
import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import tdp.bikum.antube.adapters.VideoAdapter;
import tdp.bikum.antube.models.Video;
import tdp.bikum.antube.services.PlaybackService;
import tdp.bikum.antube.utils.SessionManager;

@UnstableApi
public class MainActivity extends AppCompatActivity implements VideoAdapter.OnVideoClickListener {

    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private boolean isAskingPermission = false;
    private ExoPlayer player;
    private PlayerView playerView;
    private RecyclerView videoRecyclerView;
    private VideoAdapter videoAdapter;
    private List<Video> videoList = new ArrayList<>();
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;
    private com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigationView;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity", "onCreate: MainActivity bắt đầu");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Log.d("MainActivity", "onCreate: setContentView đã được gọi");

        initializeViews();
        setupMediaPicker();
        checkLoginAndPermissions();
        Log.d("MainActivity", "onCreate: MainActivity onCreate hoàn thành");
    }

    private void initializeViews() {
        player = new ExoPlayer.Builder(this).build();
        playerView = findViewById(R.id.player_view);
        playerView.setPlayer(player);
        Log.d("MainActivity", "initializeViews: player và playerView đã được khởi tạo");

        videoRecyclerView = findViewById(R.id.video_recycler_view);
        videoRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        Log.d("MainActivity", "initializeViews: bottomNavigationView đã được khởi tạo");

        setupPlayerListener();
    }

    private void setupPlayerListener() {
        player.setPlayWhenReady(true);
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                Log.d("MainActivity", "onPlaybackStateChanged: state = " + state);
                if (state == Player.STATE_READY) {
                    Log.d("MainActivity", "onPlaybackStateChanged: STATE_READY, gọi startForegroundService");
                    startForegroundService();
                }
            }
        });
        Log.d("MainActivity", "setupPlayerListener: addListener cho player đã được gọi");
    }

    private void setupMediaPicker() {
        pickMediaLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                Log.d("MainActivity", "setupMediaPicker: Selected URI: " + uri);
                playVideo(uri.toString());
            } else {
                Log.d("MainActivity", "setupMediaPicker: No media selected");
                Toast.makeText(this, "Không có video nào được chọn", Toast.LENGTH_SHORT).show();
            }
        });
        Log.d("MainActivity", "setupMediaPicker: pickMediaLauncher đã được khởi tạo");
    }

    private void checkLoginAndPermissions() {
        sessionManager = new SessionManager(this);
        Log.d("MainActivity", "checkLoginAndPermissions: Kiểm tra đăng nhập...");
        if (!sessionManager.isLoggedIn()) {
            Log.d("MainActivity", "checkLoginAndPermissions: Chưa đăng nhập, chuyển hướng đến LoginActivity");
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            Log.d("MainActivity", "checkLoginAndPermissions: finish() đã được gọi vì chưa đăng nhập");
            return;
        }
        Log.d("MainActivity", "checkLoginAndPermissions: Đã đăng nhập");

        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        Log.d("MainActivity", "checkAndRequestPermissions: Bắt đầu kiểm tra quyền");
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                Manifest.permission.READ_MEDIA_VIDEO : // Recommended for Android 13+ for video
                Manifest.permission.READ_EXTERNAL_STORAGE; // For older versions and general storage access (if needed)
        Log.d("MainActivity", "checkAndRequestPermissions: Quyền cần kiểm tra: " + permission);

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MainActivity", "checkAndRequestPermissions: Chưa có quyền " + permission);
            if (!isAskingPermission) {
                isAskingPermission = true;
                showPermissionExplanationDialog(permission);
            } else {
                Log.d("MainActivity", "checkAndRequestPermissions: Đang trong quá trình xin quyền, bỏ qua yêu cầu mới.");
            }
        } else {
            Log.d("MainActivity", "checkAndRequestPermissions: Đã có quyền " + permission);
            initializeVideoComponents();
        }
    }

    private void showPermissionExplanationDialog(String permission) {
        Log.d("MainActivity", "showPermissionExplanationDialog: Hiển thị dialog giải thích quyền cho " + permission);
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cần quyền truy cập")
                .setMessage("Ứng dụng cần quyền truy cập vào bộ nhớ để hiển thị video của bạn. " +
                        "Vui lòng cấp quyền để tiếp tục sử dụng ứng dụng.")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    Log.d("MainActivity", "showPermissionExplanationDialog: Người dùng đồng ý, yêu cầu quyền " + permission);
                    dialog.dismiss();
                    requestPermission(permission);
                })
                .setNegativeButton("Từ chối", (dialog, which) -> {
                    Log.d("MainActivity", "showPermissionExplanationDialog: Người dùng từ chối quyền " + permission);
                    dialog.dismiss();
                    Toast.makeText(this, "Ứng dụng cần quyền này để hoạt động", Toast.LENGTH_LONG).show();
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void requestPermission(String permission) {
        Log.d("MainActivity", "requestPermission: Yêu cầu quyền " + permission);
        ActivityCompat.requestPermissions(
                this,
                new String[]{permission},
                PERMISSION_REQUEST_READ_EXTERNAL_STORAGE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("MainActivity", "onRequestPermissionsResult: Bắt đầu xử lý kết quả yêu cầu quyền.");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        isAskingPermission = false; // Reset flag sau khi nhận kết quả

        if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE) {
            Log.d("MainActivity", "onRequestPermissionsResult: requestCode trùng khớp PERMISSION_REQUEST_READ_EXTERNAL_STORAGE.");
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "onRequestPermissionsResult: Quyền " + permissions[0] + " ĐƯỢC CẤP.");
                initializeVideoComponents();
            } else {
                Log.d("MainActivity", "onRequestPermissionsResult: Quyền " + permissions[0] + " BỊ TỪ CHỐI.");
                if (!shouldShowRequestPermissionRationale(permissions[0])) {
                    Log.d("MainActivity", "onRequestPermissionsResult: Không nên hiển thị giải thích quyền nữa, hiển thị dialog cài đặt.");
                    showSettingsDialog();
                } else {
                    Log.d("MainActivity", "onRequestPermissionsResult: Nên hiển thị giải thích quyền, toast và finish.");
                    Toast.makeText(this, "Ứng dụng cần quyền này để hoạt động", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        } else {
            Log.d("MainActivity", "onRequestPermissionsResult: requestCode KHÔNG trùng khớp PERMISSION_REQUEST_READ_EXTERNAL_STORAGE. requestCode = " + requestCode);
        }
        Log.d("MainActivity", "onRequestPermissionsResult: Kết thúc xử lý kết quả yêu cầu quyền.");
    }

    private void showSettingsDialog() {
        Log.d("MainActivity", "showSettingsDialog: Hiển thị dialog cài đặt.");
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cần quyền truy cập")
                .setMessage("Bạn đã từ chối quyền truy cập bộ nhớ cần thiết cho ứng dụng. " +
                        "Vui lòng vào Cài đặt ứng dụng để cấp quyền thủ công.")
                .setPositiveButton("Cài đặt", (dialog, which) -> {
                    Log.d("MainActivity", "showSettingsDialog: Người dùng chọn Cài đặt.");
                    dialog.dismiss();
                    openSettings();
                })
                .setNegativeButton("Đóng", (dialog, which) -> {
                    Log.d("MainActivity", "showSettingsDialog: Người dùng chọn Đóng.");
                    dialog.dismiss();
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void openSettings() {
        Log.d("MainActivity", "openSettings: Mở cài đặt ứng dụng.");
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void initializeVideoComponents() {
        Log.d("MainActivity", "initializeVideoComponents: Khởi tạo các thành phần video.");
        loadLocalVideos();
        videoAdapter = new VideoAdapter(videoList, this);
        videoRecyclerView.setAdapter(videoAdapter);
    }


    private void loadLocalVideos() {
        Log.d("MainActivity", "loadLocalVideos: Bắt đầu load video local...");
        videoList.clear();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media._ID
        };
        String sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC";

        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);

                do {
                    String title = cursor.getString(titleColumn);
                    String path = cursor.getString(dataColumn);
                    String videoId = cursor.getString(idColumn);
                    Uri thumbnailUri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoId)
                            .buildUpon()
                            .appendPath(String.valueOf(MediaStore.Video.Thumbnails.MICRO_KIND))
                            .build();

                    Video video = new Video(videoId, title, path, thumbnailUri.toString());
                    videoList.add(video);
                    Log.d("MainActivity", "loadLocalVideos: Đã thêm video: " + video.getTitle() + ", path: " + video.getPath());
                } while (cursor.moveToNext());
            }
            Log.d("MainActivity", "loadLocalVideos: Kết thúc load video local, số lượng video: " + videoList.size());
        } catch (Exception e) {
            Log.e("MainActivity", "loadLocalVideos: Lỗi khi load video local", e);
            Toast.makeText(this, "Lỗi khi tải video", Toast.LENGTH_SHORT).show();
        }
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
            bottomNavigationView.setVisibility(View.GONE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            showSystemUI();
            playerView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            playerView.getLayoutParams().height = (int) getResources().getDimension(R.dimen.player_view_height_portrait);
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

    @Override
    public void onVideoClick(Video video) {
        playVideo(video.getPath());
        Toast.makeText(this, "Phát video: " + video.getTitle(), Toast.LENGTH_SHORT).show();
    }
}