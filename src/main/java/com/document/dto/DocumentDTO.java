package com.document.dto;

import com.document.FileUploadStatus;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DocumentDTO {
    private Long id;
    private String userName;
    private FileUploadStatus uploadStatus;
}
