package vn.spring.blog.config;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import vn.spring.blog.helper.SecurityUtil;
import vn.spring.blog.helper.exception.ResourceNotFoundException;
import vn.spring.blog.helper.exception.UnauthorizedException;
import vn.spring.blog.model.RefreshToken;
import vn.spring.blog.model.User;
import vn.spring.blog.model.User.AccountStatus;
import vn.spring.blog.model.dto.response.ExchangeTokenResponse;
import vn.spring.blog.model.dto.response.LoginResponseDTO.UserLogin;
import vn.spring.blog.repository.UserRepository;
import vn.spring.blog.service.AvatarUrlService;
import vn.spring.blog.service.RefreshTokenService;

@Service 
@Transactional 
public class JwtService {
  public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS256;

  private final JwtEncoder jwtEncoder;
  private final RefreshTokenService refreshTokenService;
  private final UserRepository userRepository;
  private final AvatarUrlService avatarUrlService;

  public JwtService(
    JwtEncoder jwtEncoder,
    RefreshTokenService refreshTokenService,
    UserRepository userRepository,
    AvatarUrlService avatarUrlService
) {
    this.jwtEncoder = jwtEncoder;
    this.refreshTokenService = refreshTokenService;
    this.userRepository = userRepository;
    this.avatarUrlService = avatarUrlService;
}

  @Value("${jwt.access-token-validity}")
  private String accessTokenExpiration;

  @Value("${jwt.refresh-token-validity}")
  private String refreshTokenExpiration;

  public String createAccessToken(Authentication authentication, User user){
    Instant now = Instant.now();
    Instant validity = now.plus(Long.valueOf(accessTokenExpiration), ChronoUnit.SECONDS);

    String scope = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(" "));

    JwtClaimsSet claims = JwtClaimsSet.builder()
                                      .issuedAt(now)
                                      .expiresAt(validity)
                                      .subject(authentication.getName())
                                      .claim("scope", scope)
                                      .claim("id", user.getId())
                                      .build();

    JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).type("JWT").build();
    return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
  }

  public String genarateSecureToken(){
    byte[] randomBytes = new byte[64];
    SecureRandom secureRandom = new SecureRandom();
    secureRandom.nextBytes(randomBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
  }

  public String createRefreshToken(User user){
    if(user.getStatus() != AccountStatus.ACTIVE){
      throw new UnauthorizedException("Tài khoản đang bị cấm");
    }

    String token = genarateSecureToken();
    Instant now = Instant.now();
    Instant expiration = Instant.now().plusSeconds(Long.valueOf(refreshTokenExpiration));

    RefreshToken rf = new RefreshToken();
    rf.setCreateAt(now);
    rf.setExpiredAt(expiration);
    rf.setToken(token);
    rf.setUser(user);

    refreshTokenService.saveRefreshToken(rf);
    return token;
  }

  public ExchangeTokenResponse handleExchangeToken(String refreshToken){
    // sẽ có trường hợp không tìm thấy refresh token? -> bị admin xóa
    RefreshToken currentToken = this.refreshTokenService.findByToken(refreshToken);
    Instant now = Instant.now();
    if(currentToken.getExpiredAt().isBefore(now)){
      throw new ResourceNotFoundException("Refresh token đã hết hạn");
    }

    User user = currentToken.getUser();

    Instant expired = now.plusSeconds(Long.valueOf(accessTokenExpiration));
    String scope = "ROLE_" + user.getRole().getName();
    JwtClaimsSet claims = JwtClaimsSet.builder()
                                      .issuedAt(now)
                                      .expiresAt(expired)
                                      .subject(user.getEmail())
                                      .claim("id", user.getId())
                                      .claim("scope", scope)
                                      .build();
    JwsHeader header = JwsHeader.with(JWT_ALGORITHM).type("JWT").build();
    String newAccessToken = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    
    this.refreshTokenService.deleteById(currentToken.getId());
    String newRefreshToken = createRefreshToken(user);

    ExchangeTokenResponse response = new ExchangeTokenResponse();
    response.setAccessToken(newAccessToken);
    response.setRefreshToken(newRefreshToken);
    response.setTokenType("Bearer");
    response.setUser(UserLogin.builder()
                              .id(user.getId())
                              .username(user.getEmail())
                              .name(user.getName())
                              .address(user.getAddress())
                              .role(scope)
                              .avatarUrl(avatarUrlService.getAvatarUrl(user))
                              .build());
    
    return response;
  }


  public String createPasswordVerifiedToken(){ 
    int userId = SecurityUtil.getCurrentIdUserLogin().orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user đang đăng nhập"));
    User userLogin = this.userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User với id: " + userId + " không tồn tại"));
    Instant now = Instant.now();
    Instant expired = now.plusSeconds(300); // 5 phút
    String type = "password_verified";
    JwtClaimsSet claims = JwtClaimsSet.builder()
                                      .issuedAt(now)
                                      .expiresAt(expired)
                                      .subject(userLogin.getEmail())
                                      .claim("type", type)
                                      .claim("id", userLogin.getId())
                                      .build();
    JwsHeader header = JwsHeader.with(JWT_ALGORITHM).type("JWT").build();
    return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  }
}
