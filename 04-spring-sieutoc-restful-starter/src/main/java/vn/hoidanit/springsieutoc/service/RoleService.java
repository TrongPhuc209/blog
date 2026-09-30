package vn.hoidanit.springsieutoc.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import vn.hoidanit.springsieutoc.helper.exception.ResourceAlreadyExitsException;
import vn.hoidanit.springsieutoc.helper.exception.ResourceNotFoundException;
import vn.hoidanit.springsieutoc.model.Role;
import vn.hoidanit.springsieutoc.model.dto.request.RoleFilterRequestDTO;
import vn.hoidanit.springsieutoc.model.dto.response.RoleResponseDTO;
import vn.hoidanit.springsieutoc.repository.RoleRepository;
import vn.hoidanit.springsieutoc.service.specification.RoleSpecification;

@Service
@Transactional
@RequiredArgsConstructor
public class RoleService {
  private final RoleRepository roleRepository;

  public RoleResponseDTO converRoleToDTO(Role role){
    return RoleResponseDTO.builder().id(role.getId()).name(role.getName()).build();
  }

  public Role createRole(Role inputRole) {
    boolean checkName = roleRepository.existsByName(inputRole.getName());
    if (checkName) {
      throw new ResourceAlreadyExitsException("Role có tên: " + inputRole.getName() + " đã tồn tại!");
    }
    Role saveRole = roleRepository.save(inputRole);
    return saveRole;
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

  public RoleResponseDTO updateRole(Role inputRole) {
    Role currentRole = roleRepository.findById(inputRole.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Role có id " + inputRole.getId() + " không tồn tại!"));
    boolean checkExist = roleRepository.existsByNameAndIdNot(inputRole.getName(), inputRole.getId());
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
