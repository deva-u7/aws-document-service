package com.document.e2e.utils;

import com.document.FileUploadStatus;
import com.document.dto.DocumentDTO;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

public class DocumentUtils {

    public static DocumentDTO getRequest() {
        return DocumentDTO.builder()
                .userName("TestUser")
                .build();
    }

    public static DocumentDTO getExpectedResponse(String fileName, String bucketName) {
        return DocumentDTO.builder()
                .id(1L)
                .userName("TestUser")
                .uploadStatus(FileUploadStatus.COMPLETED)
                .build();
    }

    public static MockMultipartFile getMockMultipartFile(String fileName) {
        return new MockMultipartFile(
                "file",
                fileName,
                MediaType.MULTIPART_FORM_DATA_VALUE,
                "Hello, World!".getBytes());
    }
}
