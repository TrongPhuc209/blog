package vn.hoidanit.springsieutoc.model;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Setter 
@Getter 
@Table(name = "refesh_token")
@NoArgsConstructor 
@AllArgsConstructor 
public class RefreshToken {
  @Id 
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank (message = "token không được để trống")
  private String token;

  private Instant createAt;

  private Instant expiredAt;

  @ManyToOne 
  @JoinColumn (name = "user_id")
  private User user;
}
