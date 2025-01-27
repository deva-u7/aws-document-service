package com.document.e2e.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class AWSS3ConfigTest {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public static LocalStackContainer localStackContainer() {
        LocalStackContainer localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
                .withServices(LocalStackContainer.Service.S3);
        localStack.start();
        return localStack;
    }

    @Bean("S3Client")
    public S3Client getAmazonS3Client(LocalStackContainer localStackContainer) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(localStackContainer.getAccessKey(), localStackContainer.getSecretKey());
        final StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);
        URI endpointOverride = localStackContainer.getEndpointOverride(LocalStackContainer.Service.S3);
        return S3Client.builder().credentialsProvider(credentialsProvider).region(Region.of(localStackContainer.getRegion())).endpointOverride(endpointOverride).build();
    }
}