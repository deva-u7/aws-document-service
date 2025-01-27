package com.document.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URISyntaxException;

@Configuration
@Profile("!test")
public class AWSS3Config {

    @Value("${spring.s3.access-key}")
    String accessKey;

    @Value("${spring.s3.secret-key}")
    String secretKey;

    @Value("${spring.s3.region}")
    String region;

    @Bean("S3Client")
    public S3Client getAmazonS3Client() throws URISyntaxException {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);
        return S3Client.builder().region(Region.of(region)).credentialsProvider(credentialsProvider).build();
    }
}