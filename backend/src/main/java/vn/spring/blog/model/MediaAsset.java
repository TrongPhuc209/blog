package vn.spring.blog.model;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
@Entity 
@Table (name = "media_asset")
public class MediaAsset {
  @Id 
  @GeneratedValue (strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank (message = "storageKey không được để trống")
  private String storageKey;

  @NotBlank (message = "contentType không được để trống")
  private String contentType;

  @NotNull (message = "fileSize không được để trống")
  private Long fileSize;

  @NotNull(message = "createAt không được để trống")
  private Instant createAt;

  @ManyToOne 
  @JoinColumn (name = "user_id")
  private User user;
}
