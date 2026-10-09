package vn.spring.blog.service.specification;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import vn.spring.blog.model.Post;
import vn.spring.blog.model.Tag;
import vn.spring.blog.model.User;

public class PostSpecification {
  public static Specification<Post> hasTitle(String title){
    return (root, query, cb) -> {
      if(title == null) return cb.conjunction();
      return cb.like(root.get("title"), "%" + title + "%");
    };
  }

  public static Specification<Post> hasContent(String content){
    return (root, query, cb) -> {
      if(content == null) return cb.conjunction();
      return cb.like(root.get("content"), "%" + content + "%");
    };
  }

  public static Specification<Post> hasUserId(Integer UserId){
    return (root, query, cb) -> {
      if(UserId == null) return cb.conjunction();
      Join<Post, User> userJoin = root.join("user", JoinType.INNER);
      return cb.equal(userJoin.get("id"), UserId);
    };
  }

  public static Specification<Post> hasUserName(String UserName){
    return (root, query, cb) -> {
      if(UserName == null) return cb.conjunction();
      Join<Post, User> userJoin = root.join("user", JoinType.INNER);
      return cb.like(userJoin.get("name"), "%" + UserName + "%");
    };
  }

  public static Specification<Post> hasTagName(List<String> tagName){
    return (root, query, cb) -> {
      if(tagName == null) return cb.conjunction();
      Join<Post, Tag> tagJoin = root.join("tags", JoinType.INNER);
      // return tagJoin.get("name").in(postFilter.getTagName());
      return cb.in(tagJoin.get("name")).value(tagName);
    };
  }

  public static Specification<Post> createAtFromTo(Instant from, Instant to){
    return (root, query, cb) -> {
      if (from == null && to == null) {
        return cb.conjunction();
      }
      if (from == null) {
        return cb.lessThanOrEqualTo(root.get("createdAt"), to);
      }
      if (to == null) {
        return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
      }
      return cb.between(root.get("createdAt"), from, to);
    };
  }
}
