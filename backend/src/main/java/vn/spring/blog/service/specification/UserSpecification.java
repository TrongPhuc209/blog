package vn.spring.blog.service.specification;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import vn.spring.blog.model.Role;
import vn.spring.blog.model.User;
import vn.spring.blog.model.dto.request.UserFilterRequestDTO;


// public class UserSpecification {
//   public static Specification<User> hasName(UserFilterRequestDTO userFilter){
//     return (root, query, cb) -> {
//       if(userFilter.getName() == null) return cb.conjunction();
//       return cb.like(root.get("name"), "%" + userFilter.getName() + "%");
//     };
//   }
// }

public class UserSpecification {
  public static Specification<User> hasName(UserFilterRequestDTO userFilter){
    return (root, query, cb) -> {
      if(userFilter.getName() == null) return cb.conjunction();
      return cb.equal(root.get("name"), userFilter.getName());
    };
  }

  public static Specification<User> hasEmail(UserFilterRequestDTO userFilter){
    return (root, query, cb) -> {
      if(userFilter.getEmail() == null) return cb.conjunction();
      return cb.equal(root.get("email"), userFilter.getEmail());
    };
  }

  public static Specification<User> hasAddress(UserFilterRequestDTO userFilter){
    return (root, query, cb) -> {
      if(userFilter.getAddress() == null) return cb.conjunction();
      return cb.like(root.get("address"), "%" + userFilter.getAddress() + "%");
    };
  }

  // public static Specification<User> hasRole(UserFilterRequestDTO userFilter){
  //   return (root, query, cb) -> {
  //     if(userFilter.getRole() == null) return cb.conjunction();
  //     return cb.equal(root.get("role").get("name"), userFilter.getRole());
  //   };
  // }

  public static Specification<User> hasRole(UserFilterRequestDTO userFilter){
    return (root, query, cb) -> {
      if(userFilter.getRole() == null) return cb.conjunction();
      Join<User, Role> roleJoin = root.join("role", JoinType.INNER);
      return cb.equal(cb.lower(roleJoin.get("name")), userFilter.getRole()); 
    };
  }
}
