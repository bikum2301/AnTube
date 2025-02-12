package tdp.bikum.antube;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tdp.bikum.antube.models.ApiResponse;
import tdp.bikum.antube.services.ApiClient;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button sendOtpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailEditText = findViewById(R.id.forgot_password_email);
        sendOtpButton = findViewById(R.id.forgot_password_button);

        sendOtpButton.setOnClickListener(v -> requestForgotPasswordOtp());
    }

    private void requestForgotPasswordOtp() {
        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<ApiResponse<Void>> call = ApiClient.getApiService().forgotPassword(email);
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Mã OTP đã được gửi đến email của bạn.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(ForgotPasswordActivity.this, OTPVerificationActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("type", "forgot_password");
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Yêu cầu thất bại: " + (response.body() != null ? response.body().getMessage() : "Lỗi không xác định"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}