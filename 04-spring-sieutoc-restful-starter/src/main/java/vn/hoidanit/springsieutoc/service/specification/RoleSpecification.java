package vn.hoidanit.springsieutoc.service.specification;

import org.springframework.data.jpa.domain.Specification;

import vn.hoidanit.springsieutoc.model.Role;
import vn.hoidanit.springsieutoc.model.dto.request.RoleFilterRequestDTO;

public class RoleSpecification {
  public static Specification<Role> hasName(RoleFilterRequestDTO roleFilter){
    return (root, query, cb) -> {
      if(roleFilter.getName() == null) return cb.conjunction();
      return cb.like(root.get("name"), "%" + roleFilter.getName() + "%");
    };
  }
}
