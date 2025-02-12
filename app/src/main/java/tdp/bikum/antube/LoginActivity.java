package tdp.bikum.antube;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.UnstableApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tdp.bikum.antube.models.ApiResponse;
import tdp.bikum.antube.models.LoginRequest;
import tdp.bikum.antube.models.User;
import tdp.bikum.antube.services.ApiClient;
import tdp.bikum.antube.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerTextView, forgotPasswordTextView;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        emailEditText = findViewById(R.id.login_email);
        passwordEditText = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        registerTextView = findViewById(R.id.login_register_text);
        forgotPasswordTextView = findViewById(R.id.login_forgot_password_text);

        loginButton.setOnClickListener(v -> performLogin());
        registerTextView.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
        forgotPasswordTextView.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
    }

    private void performLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest loginRequest = new LoginRequest(email, password);
        Log.d("LoginActivity", "performLogin: Gọi API loginUser..."); // Log trước khi gọi API
        Call<ApiResponse<User>> call = ApiClient.getApiService().loginUser(loginRequest);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                Log.d("LoginActivity", "onResponse: Login API callback received"); // Log khi callback onResponse được gọi
                // **Áp dụng logic IF từ RegisterActivity.java**
                if (response.isSuccessful()) { // Kiểm tra response.isSuccessful()
                    Log.d("LoginActivity", "Đăng nhập thành công - response.isSuccessful(): " + response.isSuccessful());
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    Log.d("LoginActivity", "onResponse: Chuẩn bị Intent đến MainActivity"); // Log trước Intent
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    Log.d("LoginActivity", "onResponse: Bắt đầu Activity MainActivity..."); // Log trước startActivity
                    startActivity(intent);
                    Log.d("LoginActivity", "onResponse: startActivity() đã được gọi"); // Log sau startActivity
                    finish();
                    Log.d("LoginActivity", "onResponse: finish() đã được gọi"); // Log sau finish

                } else {
                    Log.e("LoginActivity", "Đăng nhập thất bại - response.isSuccessful: false, code: " + response.code());
                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Log.e("LoginActivity", "Đăng nhập thất bại - onFailure: " + t.getMessage()); // Log lỗi chi tiết
                Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}