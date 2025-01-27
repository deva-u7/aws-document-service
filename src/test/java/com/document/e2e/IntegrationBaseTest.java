package com.document.e2e;

import com.document.repository.DocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.s3.S3Client;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
@Slf4j
public class IntegrationBaseTest {

    @Value("${spring.s3.bucket}")
    public String bucketName;

    @Autowired
    public S3Client amazonS3;

    @Autowired
     DocumentRepository documentRepository;

    @BeforeEach
    public void setup() {
        amazonS3.createBucket(b -> b.bucket(bucketName));
    }

    @AfterEach
    public void cleanup() {
        // Cleanup all the objects in the bucket
        amazonS3.listObjectsV2Paginator(b -> b.bucket(bucketName))
                .stream()
                .flatMap(response -> response.contents().stream())
                .forEach(object -> amazonS3.deleteObject(b -> b.bucket(bucketName).key(object.key())));
        log.info("Deleted all objects from the bucket: {}", bucketName);

        // Delete the bucket
        amazonS3.deleteBucket(b -> b.bucket(bucketName));
        log.info("Deleted the bucket: {}", bucketName);
    }
}