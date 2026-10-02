package vn.spring.blog.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
  @Email (message = "Email không đúng định dạng")
  private String email;

  @NotBlank (message = "password không được để trống")
  @Size(min = 6, max = 50, message = "Mật khẩu phải từ 6 đến 50 ký tự")
  @Pattern (regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$", message = "Mật khẩu phải có chữ thường, chữ hoa, số và ký tự đặc biệt")
  private String password;

  @NotBlank (message = "name không được để trống")
  private String name;

  @NotBlank (message = "address không được để trống")
  private String address;
}
