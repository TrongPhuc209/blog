package vn.spring.blog.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.spring.blog.helper.ApiResponse;
import vn.spring.blog.helper.PageResponse;
import vn.spring.blog.model.User;
import vn.spring.blog.model.dto.request.UserFilterRequestDTO;
import vn.spring.blog.model.dto.request.UserRequestDTO;
import vn.spring.blog.model.dto.response.UserResponseDTO;
import vn.spring.blog.service.UserService;

@RestController
@RequiredArgsConstructor 
public class UserController {

	private final UserService userService;

	@PostMapping("/users")
	public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(@Valid @RequestBody User inputUser){
		UserResponseDTO createUser = this.userService.createUser(inputUser);
		return ApiResponse.created(createUser);
	}

	@GetMapping("/users")
	public ResponseEntity<ApiResponse<PageResponse<UserResponseDTO>>> getUsers(UserFilterRequestDTO userFilter, Pageable pageable){
		
		return ApiResponse.success(PageResponse.from(this.userService.fetchUsers(userFilter, pageable)));
	}

	@GetMapping("/users/{id}")
	public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(@PathVariable int id){
		return ApiResponse.success(this.userService.findUserById(id));
	}

	@PutMapping("/users/{id}")
	public ResponseEntity<ApiResponse<UserResponseDTO>> updateUser(@PathVariable int id, @RequestBody UserRequestDTO inputUser){
		inputUser.setId(id);
		return ApiResponse.success(this.userService.updateUser(inputUser), "Update user successfully");
	}

	@DeleteMapping("/users/{id}")
	public ResponseEntity<ApiResponse<String>> deleteUserById(@PathVariable int id){
		this.userService.deleteUserById(id);
		return ApiResponse.success(null, "User deleted successfully");
	}
}
