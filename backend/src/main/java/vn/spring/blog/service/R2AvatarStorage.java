package vn.spring.blog.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import vn.spring.blog.helper.exception.InternalServerException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class R2AvatarStorage {
  private final S3Client s3Client;
  private final String bucket;

  public R2AvatarStorage(S3Client s3Client, @Value("${app.storage.r2.bucket}") String bucket) {
    if (bucket.isBlank()) {
      throw new IllegalStateException("Thiếu R2_BUCKET");
    }
    this.s3Client = s3Client;
    this.bucket = bucket;
  }

  public void store(String key, byte[] content, String contentType) {
    try {
      s3Client.putObject(PutObjectRequest.builder()
          .bucket(bucket)
          .key(key)
          .contentType(contentType)
          .contentLength((long) content.length)
          .build(), RequestBody.fromBytes(content));
    } catch (RuntimeException e) {
      throw new InternalServerException("Không thể lưu avatar lên R2");
    }
  }

  public void delete(String key) {
    try {
      s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
    } catch (RuntimeException ignored) {
      // Best-effort cleanup after a failed database write.
    }
  }
}
