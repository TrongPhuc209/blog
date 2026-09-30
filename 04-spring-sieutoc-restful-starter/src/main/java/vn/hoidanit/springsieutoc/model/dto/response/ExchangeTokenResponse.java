package vn.hoidanit.springsieutoc.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.hoidanit.springsieutoc.model.dto.response.LoginResponseDTO.UserLogin;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class ExchangeTokenResponse {

  private String accessToken;

  private String refreshToken;

  private String tokenType = "Bearer";

  private UserLogin user;
}
