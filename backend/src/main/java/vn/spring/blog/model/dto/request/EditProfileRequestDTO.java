package vn.spring.blog.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class EditProfileRequestDTO {
  @NotBlank(message = "Tên hiển thị không được để trống")
  @Size(max = 255, message = "Tên hiển thị tối đa 255 ký tự")
  private String name;

  @NotBlank(message = "Địa chỉ không được để trống")
  @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
  private String address;
}
