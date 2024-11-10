package com.shopbee.paymentservice.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum TransactionStatus {

    SUCCESS("00", "Giao dịch thành công"),
    SUSPICIOUS_TRANSACTION("07", "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)."),
    NOT_REGISTERED_INTERNET_BANKING("09", "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng."),
    INVALID_AUTHENTICATION("10", "Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần"),
    PAYMENT_TIMEOUT("11", "Giao dịch không thành công do: Đã hết hạn chờ thanh toán. Xin quý khách vui lòng thực hiện lại giao dịch."),
    ACCOUNT_LOCKED("12", "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa."),
    INCORRECT_OTP("13", "Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực giao dịch (OTP). Xin quý khách vui lòng thực hiện lại giao dịch."),
    TRANSACTION_CANCELLED("24", "Giao dịch không thành công do: Khách hàng hủy giao dịch"),
    INSUFFICIENT_FUNDS("51", "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch."),
    DAILY_LIMIT_EXCEEDED("65", "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày."),
    BANK_MAINTENANCE("75", "Ngân hàng thanh toán đang bảo trì."),
    TOO_MANY_WRONG_PASSWORD_ATTEMPTS("79", "Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định. Xin quý khách vui lòng thực hiện lại giao dịch"),
    OTHER_ERRORS("99", "Các lỗi khác (lỗi còn lại, không có trong danh sách mã lỗi đã liệt kê)");

    private final String code;
    private final String description;

    TransactionStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TransactionStatus fromCode(String code) {
        return Arrays.stream(TransactionStatus.values()).filter(status -> status.code.equals(code)).findFirst().orElse(null);
    }
}

