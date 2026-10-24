package vn.spring.blog.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

@Configuration
public class R2StorageConfiguration {
  @Bean(destroyMethod = "close")
  S3Client r2S3Client(
      @Value("${app.storage.r2.endpoint}") String endpoint,
      @Value("${app.storage.r2.access-key-id}") String accessKeyId,
      @Value("${app.storage.r2.secret-access-key}") String secretAccessKey) {
    URI endpointUri = URI.create(endpoint);
    if (!"https".equalsIgnoreCase(endpointUri.getScheme()) || endpointUri.getHost() == null
        || (endpointUri.getPath() != null && !endpointUri.getPath().isBlank())) {
      throw new IllegalStateException("R2_ENDPOINT phải là S3 endpoint HTTPS, không kèm tên bucket hoặc đường dẫn");
    }
    if (accessKeyId.isBlank() || secretAccessKey.isBlank()) {
      throw new IllegalStateException("Thiếu R2_ACCESS_KEY_ID hoặc R2_SECRET_ACCESS_KEY");
    }
    return S3Client.builder()
        .endpointOverride(endpointUri)
        .region(Region.of("auto"))
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
        .serviceConfiguration(S3Configuration.builder()
            .pathStyleAccessEnabled(true)
            .chunkedEncodingEnabled(false)
            .build())
        .build();
  }
}
