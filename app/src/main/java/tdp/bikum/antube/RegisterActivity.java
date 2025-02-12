package tdp.bikum.antube;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Thêm import Log
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tdp.bikum.antube.models.ApiResponse;
import tdp.bikum.antube.models.RegisterRequest;
import tdp.bikum.antube.models.User;
import tdp.bikum.antube.services.ApiClient;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button registerButton;
    private TextView loginTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailEditText = findViewById(R.id.register_email);
        passwordEditText = findViewById(R.id.register_password);
        registerButton = findViewById(R.id.register_button);
        loginTextView = findViewById(R.id.register_login_text);

        registerButton.setOnClickListener(v -> performRegister());
        loginTextView.setOnClickListener(v -> finish()); // Quay lại LoginActivity
    }

    private void performRegister() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        RegisterRequest registerRequest = new RegisterRequest(email, password);
        Call<ApiResponse<User>> call = ApiClient.getApiService().registerUser(registerRequest);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                Log.d("RegisterActivity", "Register API onResponse called");
                // **Simplified if condition - only check response.isSuccessful()**
                if (response.isSuccessful()) {
                    Log.d("RegisterActivity", "Đăng ký thành công - response.isSuccessful(): " + response.isSuccessful());
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công. Vui lòng kiểm tra email để xác thực OTP.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(RegisterActivity.this, OTPVerificationActivity.class);
                    intent.putExtra("email", email); // Truyền email để xác thực OTP
                    intent.putExtra("type", "register"); // Loại xác thực: đăng ký
                    Log.d("RegisterActivity", "Chuẩn bị startActivity(OTPVerificationActivity)...");
                    startActivity(intent);
                    finish();
                } else {
                    Log.e("RegisterActivity", "Đăng ký thất bại - response.isSuccessful: false, code: " + response.code());
                    Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + (response.body() != null ? response.body().getMessage() : "Lỗi không xác định"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Log.e("RegisterActivity", "Đăng ký thất bại - onFailure: " + t.getMessage()); // Log lỗi chi tiết
                Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}