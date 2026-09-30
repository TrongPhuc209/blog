package vn.hoidanit.springsieutoc.service.specification;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import vn.hoidanit.springsieutoc.model.Comment;
import vn.hoidanit.springsieutoc.model.Post;
import vn.hoidanit.springsieutoc.model.dto.request.CommentFilterRequestDTO;

public class CommentSpecification {
  public static Specification<Comment> hasApproved(CommentFilterRequestDTO commentFilter){
    return (root, query, cb) -> {
      if(commentFilter.getIsApproved() == null) return cb.conjunction();
      return cb.equal(root.get("isApproved"), commentFilter.getIsApproved());
    };
  }
  public static Specification<Comment> hasPostId(CommentFilterRequestDTO commentFilter){
    return (root, query, cb) -> {
      if(commentFilter.getPostId() == null) return cb.conjunction();
      Join<Comment, Post> comJoin = root.join("post", JoinType.INNER);
      return cb.equal(comJoin.get("id"), commentFilter.getPostId());
    };
  }
  public static Specification<Comment> hasUserId(CommentFilterRequestDTO commentFilter){
    return (root, query, cb) -> {
      if(commentFilter.getUserId() == null) return cb.conjunction();
      Join<Comment, Post> comJoin = root.join("user", JoinType.INNER);
      return cb.equal(comJoin.get("id"), commentFilter.getUserId());
    };
  }



}
