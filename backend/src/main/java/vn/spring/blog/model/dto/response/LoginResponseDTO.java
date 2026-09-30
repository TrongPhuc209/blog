package vn.spring.blog.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class LoginResponseDTO {
  private String accessToken;
  private String refreshToken;
  private String tokenType = "Bearer";
  private UserLogin user;

  @Getter 
  @Setter 
  @NoArgsConstructor 
  @AllArgsConstructor 
  @Builder 
  public static class UserLogin{
    private int id;
    private String username;
    private String role;
  }
}
