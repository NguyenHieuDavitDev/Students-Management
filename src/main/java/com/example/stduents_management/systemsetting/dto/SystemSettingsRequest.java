package com.example.stduents_management.systemsetting.dto;

import jakarta.validation.constraints.Pattern;
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

    @Pattern(
            regexp = "^$|^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$",
            message = "Email không hợp lệ (để trống nếu không dùng)"
    )
    @Size(max = 255, message = "Email tối đa 255 ký tự")
    private String contactEmail;

    @Pattern(
            regexp = "^$|^https?://.+",
            message = "Website phải để trống hoặc bắt đầu bằng http:// hoặc https://"
    )
    @Size(max = 500, message = "Website tối đa 500 ký tự")
    private String website;

    @Size(max = 500, message = "Ghi chú footer tối đa 500 ký tự")
    private String footerNote;

    @Pattern(
            regexp = "^$|^https?://.+|^/.+",
            message = "URL logo: để trống, đường dẫn bắt đầu bằng / hoặc link http(s)://"
    )
    @Size(max = 500, message = "URL logo tối đa 500 ký tự")
    private String logoUrl;

    @Size(max = 500, message = "Dòng giới thiệu đăng nhập tối đa 500 ký tự")
    private String loginTagline;

    @Size(max = 1000, message = "Nội dung thông báo tối đa 1000 ký tự")
    private String globalNotice;

    private boolean globalNoticeEnabled;
}
