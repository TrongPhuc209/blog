package vn.spring.blog.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import vn.spring.blog.helper.SecurityUtil;
import vn.spring.blog.helper.exception.BadRequestException;
import vn.spring.blog.helper.exception.ResourceAlreadyExitsException;
import vn.spring.blog.helper.exception.ResourceNotFoundException;
import vn.spring.blog.helper.exception.UnauthorizedException;
import vn.spring.blog.model.Role;
import vn.spring.blog.model.User;
import vn.spring.blog.model.User.AccountStatus;
import vn.spring.blog.model.dto.request.SuspendedAccountRequestDTO;
import vn.spring.blog.model.dto.request.UserFilterRequestDTO;
import vn.spring.blog.model.dto.request.UserRequestDTO;
import vn.spring.blog.model.dto.response.RoleResponseDTO;
import vn.spring.blog.model.dto.response.UserResponseDTO;
import vn.spring.blog.repository.RoleRepository;
import vn.spring.blog.repository.UserRepository;
import vn.spring.blog.service.specification.UserSpecification;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final AvatarUrlService avatarUrlService;

	public UserResponseDTO converUserToDTO(User user) {
		return UserResponseDTO.builder()
				.id(user.getId())
				.name(user.getName())
				.email(user.getEmail())
				.address(user.getAddress())
				.avatarUrl(avatarUrlService.getAvatarUrl(user))
				.role(RoleResponseDTO.builder().id(user.getRole().getId())
						.name(user.getRole().getName())
						.build())
				.build();
	}

	public Page<UserResponseDTO> fetchUsers(UserFilterRequestDTO userFilter, Pageable pageable) {
		Specification<User> speci = Specification.allOf(UserSpecification.hasName(userFilter),
				UserSpecification.hasEmail(userFilter),
				UserSpecification.hasAddress(userFilter),
				UserSpecification.hasRole(userFilter));
		Page<UserResponseDTO> userList = this.userRepository
				.findAll(speci, pageable)
				.map(user -> UserResponseDTO.builder()
						.id(user.getId())
						.email(user.getEmail())
						.name(user.getName())
						.address(user.getAddress())
						.avatarUrl(avatarUrlService.getAvatarUrl(user))
						.role(new RoleResponseDTO(user.getRole().getId(), user.getRole().getName()))
						.build());
		return userList;
	}

	public UserResponseDTO createUser(UserRequestDTO inputUser) {
		boolean checkExist = userRepository.existsByEmail(inputUser.getEmail());
		if (checkExist) {
			throw new ResourceAlreadyExitsException("Email: " + inputUser.getEmail() + " đã tồn tại!");
		}
		User newUser = new User();
		Role roleInDB = roleRepository.findByIdOrName(inputUser.getRole().getId(), inputUser.getRole().getName())
				.orElseThrow(() -> new ResourceNotFoundException("Role không tồn tại!!"));
				
		String passwordEncode = passwordEncoder.encode(inputUser.getPassword());
		
		newUser.setEmail(inputUser.getEmail());
		newUser.setName(inputUser.getName());
		newUser.setPassword(passwordEncode);		
		newUser.setRole(roleInDB);
		newUser.setAddress(inputUser.getAddress());
		newUser.setStatus(AccountStatus.ACTIVE);

		this.userRepository.save(newUser);
		return converUserToDTO(newUser);
	}

	public UserResponseDTO findUserById(int id) {
		return converUserToDTO(this.userRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id)));
	}

	public List<UserResponseDTO> findUserByRole(String nameRole) {
		List<UserResponseDTO> listUser = userRepository
				.findAllByRole_Name(nameRole)
				.stream()
				.map(user -> UserResponseDTO.builder()
						.id(user.getId())
						.email(user.getEmail())
						.name(user.getName())
						.address(user.getAddress())
						.avatarUrl(avatarUrlService.getAvatarUrl(user))
						.role(new RoleResponseDTO(user.getRole().getId(), user.getRole().getName()))
						.build())
				.collect(Collectors.toList());
		return listUser;
	}

	public UserResponseDTO updateUser(UserRequestDTO inputUser) {
		User currentUserInDB = userRepository.findById(inputUser.getId())
				.orElseThrow(() -> new ResourceNotFoundException("User với id: " + inputUser.getId() + " không tồn tại!!!"));
		Role roleInDB = roleRepository.findByIdOrName(inputUser.getRole().getId(), inputUser.getRole().getName())
				.orElseThrow(() -> new ResourceNotFoundException("Role không tồn tại!!"));
		if (currentUserInDB != null) {
			currentUserInDB.setName(inputUser.getName());
			currentUserInDB.setEmail(inputUser.getEmail());
			currentUserInDB.setAddress(inputUser.getAddress());
			currentUserInDB.setRole(roleInDB);

			this.userRepository.save(currentUserInDB);
		}
		return converUserToDTO(currentUserInDB);
	}

	public void deleteUserById(int id) {
		this.userRepository.deleteById(id);
	}


	public void suspendedAccount(int userId, SuspendedAccountRequestDTO requestDTO){
    User targetUser = getUserByIdOrElseThrow(userId);

		Instant suspendedUntil = requestDTO.getSuspendedUntil();

    if(suspendedUntil != null && suspendedUntil.isBefore(Instant.now())) {
      throw new BadRequestException("Thời gian kết thúc phải ở tương lai");
    }
    
    targetUser.setStatus(AccountStatus.SUSPENDED);
    targetUser.setSuspendedUntil(requestDTO.getSuspendedUntil());
    targetUser.setRestrictionReason(requestDTO.getReason());
    targetUser.setRestrictionNote(requestDTO.getReasonDetails());

    userRepository.save(targetUser);
  }

  public void unsuspendedAccount(int userId){
    User targetUser = getUserByIdOrElseThrow(userId);
    targetUser.setStatus(AccountStatus.ACTIVE);
    targetUser.setSuspendedUntil(null);
    targetUser.setRestrictionReason(null);
    targetUser.setRestrictionNote(null);

    userRepository.save(targetUser);
  }

	public User findUserByEmai(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("Email not found from service"));
	}

	public User getUserByIdOrElseThrow(int userId){
		return this.userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User có id " + userId + " không tồn tại"));
	}

	public User getCurrentUser(){
		int userId = SecurityUtil.getCurrentIdUserLogin().orElseThrow(() -> new UnauthorizedException("không xác định được người dùng đang đăng nhập"));
		return getUserByIdOrElseThrow(userId);
	}

}
