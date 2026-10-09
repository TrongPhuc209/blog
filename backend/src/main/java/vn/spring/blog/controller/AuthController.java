package vn.spring.blog.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.spring.blog.config.JwtService;
import vn.spring.blog.helper.ApiResponse;
import vn.spring.blog.helper.exception.ResourceNotFoundException;
import vn.spring.blog.model.RefreshToken;
import vn.spring.blog.model.User;
import vn.spring.blog.model.dto.request.EditProfileRequestDTO;
import vn.spring.blog.model.dto.request.EnterPasswordRequestDTO;
import vn.spring.blog.model.dto.request.LoginResquestDTO;
import vn.spring.blog.model.dto.request.RegisterRequestDTO;
import vn.spring.blog.model.dto.response.ExchangeTokenResponse;
import vn.spring.blog.model.dto.response.LoginResponseDTO;
import vn.spring.blog.repository.UserRepository;
import vn.spring.blog.service.AuthService;
import vn.spring.blog.service.RefreshTokenService;

@RestController
@RequiredArgsConstructor 
@Slf4j 
public class AuthController {

	private final JwtService jwtService;	
	private final UserRepository userRepository;
	private final RefreshTokenService refreshTokenService;
	private final AuthService authService;

	@Value("${jwt.refresh-token-validity}")
	private Long expiredRefreshToken;

	@PostMapping ("auth/login")
	public ResponseEntity<?> postLogin(@Valid @RequestBody LoginResquestDTO dto) {
		LoginResponseDTO response = authService.login(dto);

		ResponseCookie resCookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
																							.httpOnly(true)
																							.secure(true)
																							.path("/")
																							.maxAge(expiredRefreshToken)
																							.build();

		ApiResponse<LoginResponseDTO> finalData = new ApiResponse<>(HttpStatus.OK, null, response, null);
		
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, resCookie.toString()).body(finalData);
	}

	@PostMapping("/auth/refresh")
	public ResponseEntity<?> postRefresh(@RequestParam String token){
		return ApiResponse.success(this.jwtService.handleExchangeToken(token));
	}

	@PostMapping("/auth/refresh-with-cookie")
	public ResponseEntity<?> postRefreshTokenWithCookie(@CookieValue(required = false) String refreshToken){
		ExchangeTokenResponse res = jwtService.handleExchangeToken(refreshToken);

		ResponseCookie resCookie = ResponseCookie.from("refreshToken", res.getRefreshToken())
																						.httpOnly(true)
																						.secure(true)
																						.path("/")
																						.maxAge(expiredRefreshToken)
																						.build();
		ApiResponse<ExchangeTokenResponse> finalData = new ApiResponse<ExchangeTokenResponse>(HttpStatus.OK, null, res, null);
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, resCookie.toString()).body(finalData);															
	}

	@GetMapping("/auth/account")
	public ResponseEntity<?> getAccount(){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Jwt jwt = (Jwt) auth.getPrincipal();

		String userId = jwt.getClaimAsString("id");
		String scope = jwt.getClaimAsString("scope");
		String username = jwt.getSubject();

		LoginResponseDTO.UserLogin user = new LoginResponseDTO.UserLogin();
		user.setId(Integer.valueOf(userId));
		user.setRole(scope);
		user.setUsername(username);

		return ApiResponse.success(user);
	}

	@PostMapping("/auth/logout")
	public ResponseEntity<?> postLogout(@CookieValue String refreshToken){
		RefreshToken currentToken = refreshTokenService.findByToken(refreshToken);
		refreshTokenService.deleteById(currentToken.getId());
		ResponseCookie cookie = ResponseCookie.from("refreshToken", null)
																					.secure(true)
																					.httpOnly(true)
																					.maxAge(0)
																					.path("/")
																					.build();
		ApiResponse<String> finalData = new ApiResponse<String>(HttpStatus.OK, null, "ok", null);
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(finalData);
	}

	@PostMapping ("/auth/delete-refresh-token")
	public ResponseEntity<?> deleteRefreshToken(@RequestParam int userId){
		User currentUser = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User với id: " + userId + " không tồn tại"));
		List<RefreshToken> listToken = refreshTokenService.findAllByUserId(currentUser.getId());
		if(listToken.isEmpty()){
			return ApiResponse.success("Không có refresh token nào để xóa");
		}
		for(RefreshToken token : listToken){
			refreshTokenService.deleteById(token.getId());
		}
		return ApiResponse.success("Xóa tất cả refresh token của userId: " + userId + " thành công");
	}

	@PostMapping("/auth/register")
	public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO request){
		this.authService.registerUser(request);
		return ApiResponse.success("");
	}

	@PostMapping("/auth/edit-profile")
	public ResponseEntity<?> editProfile(@Valid @RequestBody EditProfileRequestDTO request){
		this.authService.editProfile(request);
		return ApiResponse.success("");
	}

	@PostMapping ("/auth/verify-password")
	public ResponseEntity<?> verifyPassword(@Valid @RequestBody EnterPasswordRequestDTO request){
		boolean isMatch = this.authService.checkPassword(request);
		if(isMatch){
			String token = this.jwtService.createPasswordVerifiedToken();
			return ApiResponse.success(token);
		} else{
			return ApiResponse.error(HttpStatus.UNAUTHORIZED, "Mật khẩu không chính xác", null);
		}
	}

	@PostMapping("/auth/change-password")
	public ResponseEntity<?> changePasswordWithToken(@Valid @RequestBody EnterPasswordRequestDTO newPassword){
		return ApiResponse.success(this.authService.changePasswordWithToken(newPassword));
	}

}
