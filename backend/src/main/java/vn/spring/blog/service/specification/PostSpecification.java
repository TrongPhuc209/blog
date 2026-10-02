package vn.spring.blog.service.specification;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import vn.spring.blog.model.Post;
import vn.spring.blog.model.Tag;
import vn.spring.blog.model.User;
import vn.spring.blog.model.dto.request.PostFilterRequestDTO;

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
      if (postFilter.getFrom() == null && postFilter.getTo() == null) {
        return cb.conjunction();
      }
      if (postFilter.getFrom() == null) {
        return cb.lessThanOrEqualTo(root.get("createdAt"), postFilter.getTo());
      }
      if (postFilter.getTo() == null) {
        return cb.greaterThanOrEqualTo(root.get("createdAt"), postFilter.getFrom());
      }
      return cb.between(root.get("createdAt"), postFilter.getFrom(), postFilter.getTo());
    };
  }
}
