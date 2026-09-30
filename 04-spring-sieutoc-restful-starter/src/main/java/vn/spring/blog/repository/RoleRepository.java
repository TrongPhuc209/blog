package vn.spring.blog.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.spring.blog.model.Role;


@Repository 
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role>{
  boolean existsByName(String name);
  boolean existsByNameAndIdNot(String name, Long id);
  Optional<Role> findByIdOrName(Long id, String name);

  Optional<Role> findByName(String name);
}
