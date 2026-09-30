package vn.hoidanit.springsieutoc.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import vn.hoidanit.springsieutoc.helper.exception.ResourceAlreadyExitsException;
import vn.hoidanit.springsieutoc.helper.exception.ResourceNotFoundException;
import vn.hoidanit.springsieutoc.model.Role;
import vn.hoidanit.springsieutoc.model.User;
import vn.hoidanit.springsieutoc.model.dto.request.UserRequestDTO;
import vn.hoidanit.springsieutoc.model.dto.request.RegisterRequestDTO;
import vn.hoidanit.springsieutoc.model.dto.request.UserFilterRequestDTO;
import vn.hoidanit.springsieutoc.model.dto.response.RoleResponseDTO;
import vn.hoidanit.springsieutoc.model.dto.response.UserResponseDTO;
import vn.hoidanit.springsieutoc.repository.RoleRepository;
import vn.hoidanit.springsieutoc.repository.UserRepository;
import vn.hoidanit.springsieutoc.service.specification.UserSpecification;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	public UserResponseDTO converUserToDTO(User user) {
		return UserResponseDTO.builder()
				.id(user.getId())
				.name(user.getName())
				.email(user.getEmail())
				.address(user.getAddress())
				.role(RoleResponseDTO.builder().id(user.getRole().getId())
						.name(user.getRole().getName())
						.build())
				.build();
	}

	public Page<UserResponseDTO> fetchUsers(UserFilterRequestDTO userFilter, Pageable pageable) {
		Specification<User> speci = Specification.allOf(UserSpecification.hasName(userFilter),
																										UserSpecification.hasEmail(userFilter),
																										UserSpecification.hasAddress(userFilter),
																										UserSpecification.hasRole(userFilter)
																									);
		Page<UserResponseDTO> userList = this.userRepository
				.findAll(speci, pageable)
				.map(user -> UserResponseDTO.builder()
						.id(user.getId())
						.email(user.getEmail())
						.name(user.getName())
						.address(user.getAddress())
						.role(new RoleResponseDTO(user.getRole().getId(), user.getRole().getName()))
						.build());
		return userList;
	}

	public UserResponseDTO createUser(User inputUser) {
		boolean checkExist = userRepository.existsByEmail(inputUser.getEmail());
		if (checkExist) {
			throw new ResourceAlreadyExitsException("Email: " + inputUser.getEmail() + " đã tồn tại!");
		}

		Role roleInDB = roleRepository.findByIdOrName(inputUser.getRole().getId(), inputUser.getRole().getName())
				.orElseThrow(() -> new ResourceNotFoundException("Role không tồn tại!!"));
		inputUser.setRole(roleInDB);

		String passwordEncode = passwordEncoder.encode(inputUser.getPassword());
		inputUser.setPassword(passwordEncode);

		this.userRepository.save(inputUser);
		return converUserToDTO(inputUser);
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

	public User findUserByEmai(String email){
		return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Email not found from service"));
	}

	public void registerUser(RegisterRequestDTO request){
		boolean checkExist = this.userRepository.existsByEmail(request.getEmail());
		if(checkExist){
			throw new ResourceAlreadyExitsException("email " + request.getEmail() + " đã tồn tại");
		}
		String hashPassword = this.passwordEncoder.encode(request.getPassword());
		Role roleUser = this.roleRepository.findByName("USER").orElseThrow(() -> new ResourceNotFoundException("role không tồn tại"));
		User newUser = new User();
		newUser.setEmail(request.getEmail());
		newUser.setName(request.getName());
		newUser.setPassword(hashPassword);
		newUser.setAddress(request.getAddress());
		newUser.setRole(roleUser);
		userRepository.save(newUser);
	}
}
