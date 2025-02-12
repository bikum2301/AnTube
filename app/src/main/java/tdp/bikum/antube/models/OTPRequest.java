package tdp.bikum.antube.models;

public class OTPRequest {
    private String email;
    private String otp;
    private String type;

    public OTPRequest(String email, String otp, String type) {
        this.email = email;
        this.otp = otp;
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public String getOtp() {
        return otp;
    }

    public String getType() {
        return type;
    }
}