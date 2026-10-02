package vn.spring.blog.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class TagRequestDTO {
  @NotBlank(message = "Tên tag không được để trống")
  @Pattern(regexp = "^[^,]+$", message = "Tên tag không được chứa dấu phẩy (,)")
  private String name;
}
