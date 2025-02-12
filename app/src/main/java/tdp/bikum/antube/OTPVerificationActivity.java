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
import tdp.bikum.antube.models.OTPRequest;
import tdp.bikum.antube.models.User;
import tdp.bikum.antube.services.ApiClient;
import tdp.bikum.antube.utils.SessionManager;

public class OTPVerificationActivity extends AppCompatActivity {

    private EditText otpEditText;
    private Button verifyButton;
    private TextView resendOtpTextView;
    private String email, type;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpverification);

        sessionManager = new SessionManager(this);

        otpEditText = findViewById(R.id.otp_edit_text);
        verifyButton = findViewById(R.id.verify_otp_button);
        resendOtpTextView = findViewById(R.id.resend_otp_text);

        email = getIntent().getStringExtra("email");
        type = getIntent().getStringExtra("type"); // "register" hoặc "forgot_password"
        Log.d("OTPVerificationActivity", "onCreate: Email from Intent: " + email + ", Type from Intent: " + type);

        verifyButton.setOnClickListener(v -> verifyOTP());
        resendOtpTextView.setOnClickListener(v -> resendOTP()); // TODO: Implement resend OTP logic
    }

    private void verifyOTP() {
        String otp = otpEditText.getText().toString().trim();

        if (otp.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        OTPRequest otpRequest = new OTPRequest(email, otp, type);
        Log.d("OTPVerificationActivity", "verifyOTP: Gọi API verifyOtp - Email: " + email + ", OTP: " + otp + ", Type: " + type);
        Call<ApiResponse<User>> call = ApiClient.getApiService().verifyOtp(otpRequest);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                Log.d("OTPVerificationActivity", "verifyOTP onResponse called"); // Log khi onResponse được gọi
                // **Áp dụng logic IF từ RegisterActivity.java**
                if (response.isSuccessful()) { // Kiểm tra response.isSuccessful()
                    Log.d("OTPVerificationActivity", "Xác thực OTP thành công - response.isSuccessful(): " + response.isSuccessful());
                    Toast.makeText(OTPVerificationActivity.this, "Xác thực OTP thành công!", Toast.LENGTH_SHORT).show();
                    Log.d("OTPVerificationActivity", "onResponse: Chuẩn bị Intent đến MainActivity"); // Log trước Intent
                    Intent intent;
                    if (type.equals("register")) {
                        // Sau khi đăng ký thành công, chuyển đến MainActivity
                        sessionManager.saveUserSession(response.body().getData().getId(), response.body().getData().getEmail()); // Lưu session
                        Log.d("OTPVerificationActivity", "Xác thực OTP thành công (register), chuyển sang MainActivity"); // Log debug
                        intent = new Intent(OTPVerificationActivity.this, MainActivity.class);
                    } else if (type.equals("forgot_password")) {
                        // Sau khi xác thực OTP quên mật khẩu, TODO: Chuyển đến màn hình đặt lại mật khẩu (chưa implement)
                        Log.d("OTPVerificationActivity", "Xác thực OTP thành công (forgot_password), TODO: Chuyển đến màn hình đặt lại mật khẩu"); // Log debug
                        Toast.makeText(OTPVerificationActivity.this, "Xác thực OTP thành công. Bạn có thể đặt lại mật khẩu.", Toast.LENGTH_SHORT).show();
                        intent = new Intent(OTPVerificationActivity.this, LoginActivity.class); // Tạm thời chuyển về LoginActivity
                        // TODO: Chuyển đến màn hình đặt lại mật khẩu
                    } else {
                        // Trường hợp type không hợp lệ (không nên xảy ra)
                        Log.e("OTPVerificationActivity", "Lỗi: Loại xác thực không hợp lệ (type = " + type + ")");
                        return; // Không chuyển Activity nếu type không hợp lệ
                    }
                    Log.d("OTPVerificationActivity", "onResponse: Bắt đầu Activity tiếp theo..."); // Log trước startActivity
                    startActivity(intent);
                    Log.d("OTPVerificationActivity", "onResponse: startActivity() đã được gọi"); // Log sau startActivity
                    finish();
                    Log.d("OTPVerificationActivity", "onResponse: finish() đã được gọi"); // Log sau finish

                } else {
                    Log.e("OTPVerificationActivity", "Xác thực OTP thất bại - response.isSuccessful: false, code: " + response.code());
                    Toast.makeText(OTPVerificationActivity.this, "Xác thực OTP thất bại: Mã OTP không đúng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Log.e("OTPVerificationActivity", "Xác thực OTP thất bại - onFailure: " + t.getMessage()); // Log lỗi chi tiết
                Toast.makeText(OTPVerificationActivity.this, "Xác thực OTP thất bại: Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resendOTP() {
        // TODO: Implement logic to resend OTP (call backend API to resend OTP)
        Toast.makeText(this, "Tính năng gửi lại OTP chưa được triển khai.", Toast.LENGTH_SHORT).show();
    }
}