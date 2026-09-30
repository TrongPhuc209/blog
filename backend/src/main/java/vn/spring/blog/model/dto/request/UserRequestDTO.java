package vn.spring.blog.model.dto.request;

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
public class UserRequestDTO {
	private int id;
	private String name;
	private String email;
	private String address;
	private RoleRequestDTO role;
}
