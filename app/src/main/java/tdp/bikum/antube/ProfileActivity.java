package tdp.bikum.antube;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tdp.bikum.antube.models.ApiResponse;
import tdp.bikum.antube.models.User;
import tdp.bikum.antube.services.ApiClient;
import tdp.bikum.antube.utils.SessionManager;

public class ProfileActivity extends AppCompatActivity {

    private TextView emailTextView, nameTextView; // Ví dụ hiển thị email và name
    private Button logoutButton;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);

        emailTextView = findViewById(R.id.profile_email);
        nameTextView = findViewById(R.id.profile_name); // Ví dụ hiển thị name
        logoutButton = findViewById(R.id.profile_logout_button);

        logoutButton.setOnClickListener(v -> logout());

        loadUserProfile();
    }

    private void loadUserProfile() {
        Call<ApiResponse<User>> call = ApiClient.getApiService().getProfile(); // API cần xác thực
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    User user = response.body().getData();
                    emailTextView.setText("Email: " + user.getEmail());
                    nameTextView.setText("Tên: " + user.getName()); // Ví dụ hiển thị name
                    // ... Hiển thị các thông tin hồ sơ khác ...
                } else {
                    Toast.makeText(ProfileActivity.this, "Không thể tải thông tin hồ sơ: " + (response.body() != null ? response.body().getMessage() : "Lỗi không xác định"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        sessionManager.clearSession(); // Xóa session
        startActivity(new Intent(ProfileActivity.this, LoginActivity.class)); // Quay lại màn hình đăng nhập
        finishAffinity(); // Đóng tất cả các activity trước đó
    }
}