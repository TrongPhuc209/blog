package vn.spring.blog.service;

import java.time.Instant;
import java.util.Objects;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import vn.spring.blog.config.JwtService;
import vn.spring.blog.helper.SecurityUtil;
import vn.spring.blog.helper.exception.ForbiddenException;
import vn.spring.blog.helper.exception.ResourceAlreadyExitsException;
import vn.spring.blog.helper.exception.ResourceNotFoundException;
import vn.spring.blog.model.Role;
import vn.spring.blog.model.User;
import vn.spring.blog.model.dto.request.EditProfileRequestDTO;
import vn.spring.blog.model.dto.request.EnterPasswordRequestDTO;
import vn.spring.blog.model.dto.request.LoginResquestDTO;
import vn.spring.blog.model.dto.request.RegisterRequestDTO;
import vn.spring.blog.model.dto.response.LoginResponseDTO;
import vn.spring.blog.repository.RoleRepository;
import vn.spring.blog.repository.UserRepository;

@Service 
@RequiredArgsConstructor 
@Transactional 
public class AuthService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final UserService userService;
  private final JwtService jwtService;	

  @Transactional (dontRollbackOn = BadCredentialsException.class)
  public LoginResponseDTO login(LoginResquestDTO dto){
    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword());
		
    Authentication authentication;
    try{
      authentication = authenticationManager.authenticate(authToken);
      resetFailedAttempts(dto.getUsername());
    } catch (BadCredentialsException ex){
      increaseFailedAttempts(dto.getUsername());
      throw ex;
    }

		User user = userService.findUserByEmai(authentication.getName());
		
		String actoken = jwtService.createAccessToken(authentication, user);
		String rfToken = jwtService.createRefreshToken(user);

		LoginResponseDTO response = new LoginResponseDTO();
		response.setAccessToken(actoken);
		response.setRefreshToken(rfToken);
		String role = authentication.getAuthorities().stream().findFirst().map(GrantedAuthority::getAuthority).orElse(null);
		response.setUser(new LoginResponseDTO.UserLogin(user.getId(), authentication.getName(), role));
    
    return response;
  }

  private void resetFailedAttempts(String username){
    userRepository.findByEmail(username).ifPresent(user -> {
      user.setFailedLoginAttempts(0);
      user.setLoginLockedUntil(null);
      user.setLoginLockCount(0);

      userRepository.save(user); 
    });
  }
  
  private void increaseFailedAttempts(String username){
    userRepository.findByEmail(username).ifPresent(user -> {
      user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
  
      if(user.getFailedLoginAttempts() >= 5){
        int count = user.getLoginLockCount() + 1;
        user.setLoginLockCount(count);
        user.setLoginLockedUntil(Instant.now().plusSeconds(900 * count));
        user.setFailedLoginAttempts(0);
      }
      userRepository.save(user);
    });
  }

  public boolean checkPassword(EnterPasswordRequestDTO request) {
    Integer userId = SecurityUtil.getCurrentIdUserLogin().orElseThrow(() -> new ResourceNotFoundException("Id không tồn tại"));
    User userOwner = userRepository.findById(Objects.requireNonNull(userId)).orElseThrow(() -> new ResourceNotFoundException("Id không tồn tại"));
    
    boolean isMatch = passwordEncoder.matches(request.getPassword(), userOwner.getPassword());
    if (isMatch) {
      return true;
    } else {
      return false;
    }
  }

  public String changePasswordWithToken(EnterPasswordRequestDTO newPassword) {
    User currentUser = userService.getCurrentUser();
    String typeAction = SecurityUtil.getCurrentTypeLogin();

    if(!"password_verified".equals(typeAction)){
      throw new ForbiddenException("Bạn không có quyền thực hiện thao tác này");
    }

    String encodedPassword = passwordEncoder.encode(newPassword.getPassword());
    currentUser.setPassword(encodedPassword);
    userRepository.save(currentUser);
    return "Đã đổi mật khẩu thành công";
  }

  public void registerUser(RegisterRequestDTO request) {
		boolean checkExist = this.userRepository.existsByEmail(request.getEmail());
		if (checkExist) {
			throw new ResourceAlreadyExitsException("email " + request.getEmail() + " đã tồn tại");
		}
		String hashPassword = this.passwordEncoder.encode(request.getPassword());
		Role roleUser = this.roleRepository.findByName("USER")
				.orElseThrow(() -> new ResourceNotFoundException("role không tồn tại"));
		User newUser = new User();
		newUser.setEmail(request.getEmail());
		newUser.setName(request.getName());
		newUser.setPassword(hashPassword);
		newUser.setAddress(request.getAddress());
		newUser.setRole(roleUser);
		userRepository.save(newUser);
	}

	public void editProfile(EditProfileRequestDTO request) {
		User loginUser = userService.getCurrentUser();
		loginUser.setName(request.getName());
		loginUser.setAddress(request.getAddress());
		this.userRepository.save(loginUser);
	}
}
