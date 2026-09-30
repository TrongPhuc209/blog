package vn.spring.blog.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.spring.blog.model.Post;
import vn.spring.blog.model.Tag;

@Repository 
public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post>{
  List<Post> findByTagsContains(Tag tag);
}
