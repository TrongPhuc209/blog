package vn.spring.blog.service.specification;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import vn.spring.blog.model.Post;
import vn.spring.blog.model.Tag;
import vn.spring.blog.model.dto.request.TagFilterRequestDTO;

public class TagSpecification {
  public static Specification<Tag> hasName(TagFilterRequestDTO tagFilter){
    return (root, query, cb) -> {
      if(tagFilter.getName() == null) return cb.conjunction();
      return cb.like(root.get("name"), "%" + tagFilter.getName() + "%");
    };
  }

  public static Specification<Tag> hasPost(TagFilterRequestDTO tagFilter){
    return (root, query, cb) -> {
      if(tagFilter.getPostId() == null) return cb.conjunction();
      Join<Tag, Post> postJoin = root.join("posts", JoinType.INNER);
      return cb.equal(postJoin.get("id"), tagFilter.getPostId());
    };
  }
}
