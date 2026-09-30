package vn.hoidanit.springsieutoc.service.specification;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import vn.hoidanit.springsieutoc.model.Post;
import vn.hoidanit.springsieutoc.model.Tag;
import vn.hoidanit.springsieutoc.model.User;
import vn.hoidanit.springsieutoc.model.dto.request.PostFilterRequestDTO;

public class PostSpecification {
  public static Specification<Post> hasTitle(PostFilterRequestDTO postFilter){
    return (root, query, cb) -> {
      if(postFilter.getTitle() == null) return cb.conjunction();
      return cb.like(root.get("title"), "%" + postFilter.getTitle() + "%");
    };
  }
  public static Specification<Post> hasContent(PostFilterRequestDTO postFilter){
    return (root, query, cb) -> {
      if(postFilter.getContent() == null) return cb.conjunction();
      return cb.like(root.get("content"), "%" + postFilter.getContent() + "%");
    };
  }
  public static Specification<Post> hasUserId(PostFilterRequestDTO postFilter){
    return (root, query, cb) -> {
      if(postFilter.getUserId() == null) return cb.conjunction();
      Join<Post, User> userJoin = root.join("user", JoinType.INNER);
      return cb.equal(userJoin.get("id"), postFilter.getUserId());
    };
  }

  public static Specification<Post> hasTagName(PostFilterRequestDTO postFilter){
    return (root, query, cb) -> {
      if(postFilter.getTagName() == null) return cb.conjunction();
      Join<Post, Tag> tagJoin = root.join("tags", JoinType.INNER);
      // return tagJoin.get("name").in(postFilter.getTagName());
      return cb.in(tagJoin.get("name")).value(postFilter.getTagName());
    };
  }

  public static Specification<Post> createAtFromTo(PostFilterRequestDTO postFilter){
    return (root, query, cb) -> {
      if(postFilter.getFrom() == null || postFilter.getTo() == null) cb.conjunction();
      return cb.between(root.get("createdAt"), postFilter.getFrom(), postFilter.getTo());
    };
  }
}
