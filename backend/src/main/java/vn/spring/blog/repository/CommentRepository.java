package vn.spring.blog.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.spring.blog.model.Comment;

@Repository 
public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {
  List<Comment> findAllByUser_Id(int id);
  List<Comment> findAllByPost_Id(Long id);

  Optional<Comment> findByIdAndPost_Id(Long parentId, Long postId);
}
