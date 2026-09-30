package vn.spring.blog.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
public class UserResponseDTO {

	private int id;

	private String name;

	private String email;

	private String address;

	private RoleResponseDTO role;

	// private List<Post> posts;

	// private List<Comment> comments;
}
