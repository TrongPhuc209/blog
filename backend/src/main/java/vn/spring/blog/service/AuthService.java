package vn.spring.blog.service;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import vn.spring.blog.config.JwtService;
import vn.spring.blog.helper.SecurityUtil;
import vn.spring.blog.helper.exception.ForbiddenException;
import vn.spring.blog.helper.exception.BadRequestException;
import vn.spring.blog.helper.exception.InternalServerException;
import vn.spring.blog.helper.exception.ResourceAlreadyExitsException;
import vn.spring.blog.helper.exception.ResourceNotFoundException;
import vn.spring.blog.model.MediaAsset;
import vn.spring.blog.model.Role;
import vn.spring.blog.model.User;
import vn.spring.blog.model.dto.request.ChangeAvatarRequestDTO;
import vn.spring.blog.model.dto.request.EditProfileRequestDTO;
import vn.spring.blog.model.dto.request.EnterPasswordRequestDTO;
import vn.spring.blog.model.dto.request.LoginResquestDTO;
import vn.spring.blog.model.dto.request.RegisterRequestDTO;
import vn.spring.blog.model.dto.response.LoginResponseDTO;
import vn.spring.blog.repository.MediaAssetRepository;
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
  private final MediaAssetRepository mediaAssetRepository;
  private final R2AvatarStorage avatarStorage;
  private final AvatarUrlService avatarUrlService;

  private static final long MAX_AVATAR_BYTES = 5L * 1024 * 1024;
  private static final int MAX_AVATAR_DIMENSION = 8000;
  private static final int AVATAR_SIZE = 300;

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
		response.setUser(LoginResponseDTO.UserLogin.builder()
        .id(user.getId())
        .username(authentication.getName())
        .name(user.getName())
        .address(user.getAddress())
        .role(role)
        .avatarUrl(avatarUrlService.getAvatarUrl(user))
        .build());
    
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
		loginUser.setName(request.getName().trim());
		loginUser.setAddress(request.getAddress().trim());
		this.userRepository.save(loginUser);
	}

  public void changeAvatar(ChangeAvatarRequestDTO request) {
    MultipartFile upload = request.getImage();
    if (upload == null || upload.isEmpty()) {
      throw new BadRequestException("Ảnh không được để trống");
    }
    if (upload.getSize() > MAX_AVATAR_BYTES) {
      throw new BadRequestException("Kích thước ảnh tối đa là 5 MB");
    }
    if (request.getX() == null || request.getY() == null || request.getWidth() == null || request.getHeight() == null
        || request.getX() < 0 || request.getY() < 0 || request.getWidth() <= 0 || request.getHeight() <= 0) {
      throw new BadRequestException("Thông tin crop ảnh không hợp lệ");
    }

    byte[] bytes;
    try {
      bytes = upload.getBytes();
    } catch (IOException e) {
      throw new BadRequestException("Không thể đọc file ảnh");
    }
    String imageFormat = detectImageFormat(bytes);
    if (!"image/jpeg".equals(upload.getContentType()) && !"image/png".equals(upload.getContentType())) {
      throw new BadRequestException("Chỉ chấp nhận ảnh JPEG hoặc PNG");
    }
    if (imageFormat == null || !imageFormat.equals(upload.getContentType())) {
      throw new BadRequestException("Nội dung file không khớp định dạng JPEG hoặc PNG đã khai báo");
    }

    BufferedImage source = decodeImage(bytes);
    long right = (long) request.getX() + request.getWidth();
    long bottom = (long) request.getY() + request.getHeight();
    if (right > source.getWidth() || bottom > source.getHeight()) {
      throw new BadRequestException("Vùng crop nằm ngoài kích thước ảnh");
    }

    BufferedImage cropped = source.getSubimage(request.getX(), request.getY(), request.getWidth(), request.getHeight());
    BufferedImage resized = resizeAvatar(cropped);
    User currentUser = userService.getCurrentUser();
    int userId = currentUser.getId();
    String storageKey = "users/" + userId + "/avatars/" + UUID.randomUUID() + ".webp";

    byte[] output;
    try {
      try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        if (!ImageIO.write(resized, "webp", outputStream)) {
        throw new InternalServerException("Không thể mã hóa ảnh sang WebP");
        }
        output = outputStream.toByteArray();
      }
    } catch (IOException e) {
      throw new InternalServerException("Không thể mã hóa ảnh sang WebP");
    }

    avatarStorage.store(storageKey, output, "image/webp");
    try {
      MediaAsset asset = MediaAsset.builder()
          .storageKey(storageKey)
          .contentType("image/webp")
          .fileSize((long) output.length)
          .createAt(Instant.now())
          .user(currentUser)
          .build();
      mediaAssetRepository.save(asset);
      currentUser.setAvatar(asset);
      userRepository.save(currentUser);
    } catch (RuntimeException e) {
      avatarStorage.delete(storageKey);
      throw e;
    }
  }

  private String detectImageFormat(byte[] bytes) {
    try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
      if (input == null) return null;
      var readers = ImageIO.getImageReaders(input);
      if (!readers.hasNext()) return null;
      var reader = readers.next();
      try {
        reader.setInput(input, true, true);
        String format = reader.getFormatName();
        if ("JPEG".equalsIgnoreCase(format) || "JPG".equalsIgnoreCase(format)) return "image/jpeg";
        if ("PNG".equalsIgnoreCase(format)) return "image/png";
        return null;
      } finally {
        reader.dispose();
      }
    } catch (IOException e) {
      return null;
    }
  }

  private BufferedImage decodeImage(byte[] bytes) {
    try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
      if (input == null) throw new BadRequestException("File ảnh không hợp lệ");
      var readers = ImageIO.getImageReaders(input);
      if (!readers.hasNext()) throw new BadRequestException("File ảnh không hợp lệ");
      var reader = readers.next();
      try {
        reader.setInput(input, true, true);
        int imageWidth = reader.getWidth(0);
        int imageHeight = reader.getHeight(0);
        if (imageWidth <= 0 || imageHeight <= 0 || imageWidth > MAX_AVATAR_DIMENSION
            || imageHeight > MAX_AVATAR_DIMENSION || (long) imageWidth * imageHeight > 40_000_000L) {
          throw new BadRequestException("Ảnh vượt giới hạn 8000 x 8000 pixel hoặc 40 megapixel");
        }
        BufferedImage image = reader.read(0);
        if (image == null) throw new BadRequestException("File ảnh không hợp lệ");
        return image;
      } finally {
        reader.dispose();
      }
    } catch (IOException e) {
      throw new BadRequestException("File ảnh không hợp lệ");
    }
  }

  private BufferedImage resizeAvatar(BufferedImage source) {
    int imageType = source.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
    BufferedImage resized = new BufferedImage(AVATAR_SIZE, AVATAR_SIZE, imageType);
    Graphics2D graphics = resized.createGraphics();
    try {
      graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      graphics.drawImage(source, 0, 0, AVATAR_SIZE, AVATAR_SIZE, null);
    } finally {
      graphics.dispose();
    }
    return resized;
  }
}
