package com.document.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class AWSS3FileService {

    private final S3Client amazonS3;

    @Value("${spring.s3.bucket}")
    String bucketName;

     public static final String S3_BASE_URL = "https://s3.amazonaws.com/";


    public Pair<String,String> fileUpload(MultipartFile multipartFile){
        log.info("Starting file upload process for file: {}", multipartFile.getOriginalFilename());
        try {
            String resourceUrl = null;
            String fileName = multipartFile.getOriginalFilename();
            String s3Key = String.format("/object/%s", fileName);

            final File file = convertMultiPartFileToFile(multipartFile);
            log.debug("Uploading file '{}' to bucket {}", file.getName(), bucketName);

            uploadResourceToS3Bucket(bucketName, file, s3Key);

            // Clean up local temporary file
            file.delete();

            log.debug("File uploaded successfully. Resource URL: {}", resourceUrl);
           return Pair.of(getResourceUrl(bucketName, s3Key),s3Key);
        } catch (AwsServiceException e) {
            log.error("AWS Service error occurred while uploading file: {}", multipartFile.getOriginalFilename(), e);
            throw new RuntimeException("AWS Service error during file upload", e);
        } catch (IOException e) {
            log.error("Error occurred while processing file upload for: {}", multipartFile.getOriginalFilename(), e);
            throw new RuntimeException("File processing error during upload", e);
        }
    }

    public ResponseInputStream<GetObjectResponse> fetchDocumentByKey(String key) {
        log.debug("Starting process to get file from bucket: {}, key: {}", bucketName, key);
        try {
            // Create the GetObjectRequest
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            log.debug("Document fetch successfully");
            // Retrieve the file from S3
            return amazonS3.getObject(getObjectRequest);
        } catch (NoSuchKeyException e) {
            log.error("File with key {} not found in bucket {}", key, bucketName, e);
            throw new RuntimeException("File not found in bucket with the specified key: " + key, e);
        } catch (SdkClientException e) {
            log.error("Error occurred while accessing S3 for key {} in bucket {}", key, bucketName, e);
            throw new RuntimeException("An error occurred while accessing the S3 bucket. Please try again later.", e);
        } catch (Exception e) {
            log.error("Unexpected error occurred while retrieving file with key {} from bucket {}", key, bucketName, e);
            throw new RuntimeException("An unexpected error occurred while retrieving the file. Please contact support.", e);
        }
    }


    private String getResourceUrl(String bucketName, String path) {
        String resourceUrl;
        resourceUrl = S3_BASE_URL + bucketName + path ;
        return resourceUrl;
    }

    private void uploadResourceToS3Bucket(String bucketName, File file, String uniqueFileNameWithPath) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(uniqueFileNameWithPath)
                .build();
        amazonS3.putObject(putObjectRequest,  RequestBody.fromFile(file));
    }

    private File convertMultiPartFileToFile(final MultipartFile multipartFile) throws IOException {
        final File file = new File("test");
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(multipartFile.getBytes());
        }
        return file;
    }
}
