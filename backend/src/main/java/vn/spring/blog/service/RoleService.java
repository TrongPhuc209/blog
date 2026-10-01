package vn.spring.blog.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import vn.spring.blog.helper.exception.ResourceAlreadyExitsException;
import vn.spring.blog.helper.exception.ResourceNotFoundException;
import vn.spring.blog.model.Role;
import vn.spring.blog.model.dto.request.RoleFilterRequestDTO;
import vn.spring.blog.model.dto.request.RoleRequestDTO;
import vn.spring.blog.model.dto.response.RoleResponseDTO;
import vn.spring.blog.repository.RoleRepository;
import vn.spring.blog.service.specification.RoleSpecification;

@Service
@Transactional
@RequiredArgsConstructor
public class RoleService {
  private final RoleRepository roleRepository;

  public RoleResponseDTO converRoleToDTO(Role role){
    return RoleResponseDTO.builder().id(role.getId()).name(role.getName()).build();
  }

  public RoleResponseDTO createRole(RoleRequestDTO inputRole) {
    boolean checkName = roleRepository.existsByName(inputRole.getName());
    if (checkName) {
      throw new ResourceAlreadyExitsException("Role có tên: " + inputRole.getName() + " đã tồn tại!");
    }
    Role saveRole = new Role();
    saveRole.setName(inputRole.getName());
    saveRole.setDescription(inputRole.getDescription());
    return converRoleToDTO(roleRepository.save(saveRole));
  }

  public Page<RoleResponseDTO> getAllRole(RoleFilterRequestDTO roleFilter, Pageable pageable) {
    Specification<Role> spec = Specification.allOf(RoleSpecification.hasName(roleFilter)
                                                  );
    Page<RoleResponseDTO> listRole = roleRepository.findAll(spec, pageable)
        .map(role -> converRoleToDTO(role));
    return listRole;
  }

  public RoleResponseDTO getRoleById(Long id) {
    Role currentRole = roleRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Role có id " + id + " không tồn tại!"));
    return converRoleToDTO(currentRole);
  }

  public RoleResponseDTO updateRole(Long id, RoleRequestDTO inputRole) {
    Role currentRole = roleRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Role có id " + id + " không tồn tại!"));
    boolean checkExist = roleRepository.existsByNameAndIdNot(inputRole.getName(), id);
    if (checkExist) {
      throw new ResourceAlreadyExitsException("Role có tên: " + inputRole.getName() + " đã tồn tại!");
    }

    currentRole.setName(inputRole.getName());
    currentRole.setDescription(inputRole.getDescription());
    roleRepository.save(currentRole);

    return converRoleToDTO(currentRole);
  }

  public void deleteRole(Long id) {
    roleRepository.deleteById(id);
  }
}
