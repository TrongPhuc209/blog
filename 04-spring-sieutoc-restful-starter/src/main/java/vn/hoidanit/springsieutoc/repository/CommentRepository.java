package vn.hoidanit.springsieutoc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.hoidanit.springsieutoc.model.Comment;

@Repository 
public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {
  List<Comment> findAllByUser_Id(int id);
  List<Comment> findAllByPost_Id(Long id);
}
