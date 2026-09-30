package vn.spring.blog.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.spring.blog.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User>{
	Optional<User> findByName(String name);

	Optional<User> findByNameAndEmail(String name, String email);
	
	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	List<User> findAllByRole_Name(String name);

	@EntityGraph(attributePaths = "role")
	Page<User> findAll(Specification<User> spec, Pageable pageable);
}
