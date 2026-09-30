package vn.spring.blog.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.spring.blog.helper.ApiResponse;
import vn.spring.blog.helper.PageResponse;
import vn.spring.blog.model.Role;
import vn.spring.blog.model.dto.request.RoleFilterRequestDTO;
import vn.spring.blog.model.dto.response.RoleResponseDTO;
import vn.spring.blog.service.RoleService;

@RestController 
@RequestMapping("/roles")
@RequiredArgsConstructor  
public class RoleController {
  private final RoleService roleService;

  @PostMapping
  public ResponseEntity<ApiResponse<Role>> createRole(@Valid @RequestBody Role inputRole){
    return ApiResponse.created(roleService.createRole(inputRole));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<PageResponse<RoleResponseDTO>>> getAllRole(RoleFilterRequestDTO roleFilter, Pageable pageable){
    return ApiResponse.success(PageResponse.from(roleService.getAllRole(roleFilter, pageable)));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<RoleResponseDTO>> getById(@PathVariable Long id){
    return ApiResponse.success(roleService.getRoleById(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<RoleResponseDTO>> updateRole(@PathVariable Long id, @Valid @RequestBody Role inputRole){
    inputRole.setId(id);
    return ApiResponse.success(roleService.updateRole(inputRole));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<String>> dateleRole(@PathVariable Long id){
    roleService.deleteRole(id);
    return ApiResponse.success("Xóa thành công");
  }
}
