package vn.spring.blog.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class RegisterRequestDTO {
  @NotBlank (message = "email không được để trống")
  private String email;

  @NotBlank (message = "password không được để trống")
  private String password;

  @NotBlank (message = "name không được để trống")
  private String name;

  @NotBlank (message = "address không được để trống")
  private String address;
}
