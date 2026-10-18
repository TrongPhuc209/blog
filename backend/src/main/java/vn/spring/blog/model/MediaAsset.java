package vn.spring.blog.model;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity 
@Table (name = "media_asset")
public class MediaAsset {
  private Long id;
  private String storageKey;
  private String contentType;
  private Long fileSize;
  private Instant createAt;
}
