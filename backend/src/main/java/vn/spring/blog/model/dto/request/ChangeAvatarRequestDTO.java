package vn.spring.blog.model.dto.request;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeAvatarRequestDTO {

  @NotNull(message = "Ảnh không được để trống")
  private MultipartFile image;

  @NotNull(message = "x không được để trống")
  @PositiveOrZero(message = "x không được âm")
  private Integer x;

  @NotNull(message = "y không được để trống")
  @PositiveOrZero(message = "y không được âm")
  private Integer y;

  @Positive(message = "width phải lớn hơn 0")
  private Integer width;

  @Positive(message = "height phải lớn hơn 0")
  private Integer height;
}
