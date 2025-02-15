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
        Log.d("LoginActivity", "performLogin: Gọi API loginUser...");
        Call<ApiResponse<User>> call = ApiClient.getApiService().loginUser(loginRequest);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                Log.d("LoginActivity", "onResponse: Login API callback received");
                if (response.isSuccessful()) {
                    Log.d("LoginActivity", "Đăng nhập thành công - response.isSuccessful(): " + response.isSuccessful());
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                    Log.d("LoginActivity", "onResponse: Response Body: " + response.body().toString());
                    // **ĐOẠN CODE THÊM VÀO ĐỂ LƯU SESSION**
                    User loggedInUser = response.body().getData(); // Lấy đối tượng User từ response
                    if (loggedInUser != null) {
                        Log.d("LoginActivity", "onResponse: User ID từ API: " + loggedInUser.getId()); // Log User ID
                        sessionManager.saveUserSession(loggedInUser.getId(), loggedInUser.getEmail()); // **LƯU SESSION VỚI USER OBJECT**
                        Log.d("LoginActivity", "onResponse: Session đã được lưu vào SessionManager"); // Log sau khi lưu session
                    } else {
                        Log.e("LoginActivity", "onResponse: Lỗi: Không nhận được thông tin User từ API response"); // Log lỗi nếu không có User
                        Toast.makeText(LoginActivity.this, "Lỗi đăng nhập: Không nhận được thông tin người dùng.", Toast.LENGTH_SHORT).show();
                        return; // Dừng lại nếu không có thông tin User
                    }
                    // **KẾT THÚC ĐOẠN CODE THÊM VÀO**


                    Log.d("LoginActivity", "onResponse: Chuẩn bị Intent đến MainActivity");
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    Log.d("LoginActivity", "onResponse: Bắt đầu Activity MainActivity...");
                    startActivity(intent);
                    Log.d("LoginActivity", "onResponse: startActivity() đã được gọi");
                    finish();
                    Log.d("LoginActivity", "onResponse: finish() đã được gọi");

                } else {
                    Log.e("LoginActivity", "Đăng nhập thất bại - response.isSuccessful: false, code: " + response.code());
                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Log.e("LoginActivity", "Đăng nhập thất bại - onFailure: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}