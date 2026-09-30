package vn.spring.blog.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.spring.blog.model.Tag;

@Repository 
public interface TagRepository extends JpaRepository<Tag, Long>, JpaSpecificationExecutor<Tag>{
  boolean existsByName(String name);

  Optional<Tag> findByIdOrName(Long id, String name);
}
