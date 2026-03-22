package com.example.stduents_management.document.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
public class DocumentRequest {

    @NotBlank
    @Size(max = 255)
    private String title;

    /** URL ngoài hoặc đường dẫn đã lưu; bỏ trống nếu chỉ tải file từ máy. */
    @Size(max = 500)
    private String fileUrl;

    @Size(max = 50)
    private String fileType;

    @NotNull
    private UUID subjectId;

    @NotNull
    private UUID uploadedById;

    @Size(max = 500)
    private String description;

    /** Form multipart / REST multipart — không dùng trong JSON thuần. */
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private MultipartFile attachmentFile;

    @AssertTrue(message = "Cần nhập URL file hoặc chọn file tải lên từ máy")
    public boolean isFileSourceValid() {
        boolean hasUrl = fileUrl != null && !fileUrl.isBlank();
        boolean hasFile = attachmentFile != null && !attachmentFile.isEmpty();
        return hasUrl || hasFile;
    }
}
