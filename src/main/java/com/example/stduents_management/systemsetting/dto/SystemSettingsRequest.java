package com.example.stduents_management.systemsetting.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemSettingsRequest {

    @Size(max = 500, message = "Tên đơn vị tối đa 500 ký tự")
    private String schoolName;

    @Size(max = 200, message = "Tên viết tắt tối đa 200 ký tự")
    private String schoolShortName;

    @Size(max = 1000, message = "Địa chỉ tối đa 1000 ký tự")
    private String address;

    @Size(max = 50, message = "Số điện thoại tối đa 50 ký tự")
    private String phone;

    @Email(message = "Email không hợp lệ")
    @Size(max = 255, message = "Email tối đa 255 ký tự")
    private String contactEmail;

    @Size(max = 500, message = "Website tối đa 500 ký tự")
    private String website;

    @Size(max = 500, message = "Ghi chú footer tối đa 500 ký tự")
    private String footerNote;
}
