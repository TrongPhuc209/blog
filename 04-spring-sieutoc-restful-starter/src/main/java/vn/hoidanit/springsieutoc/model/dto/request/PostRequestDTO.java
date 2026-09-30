package vn.hoidanit.springsieutoc.model.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class PostRequestDTO {

	@NotBlank(message = "Title không được để trống")
	private String title;

	@NotBlank(message = "Content không được để trống")
	private String content;

  @NotNull (message = "Tag không được để trống")
  @Valid 
  private List<InputTag> tag;

  @NotNull (message = "User không được để trống")
  @Valid 
  private InputUser user;

  @Getter 
  @Setter 
  @NoArgsConstructor 
  @AllArgsConstructor 
  public static class InputTag{
    @NotNull(message = "tag.id không được phép để trống")
    private Long id;

    @NotBlank(message = "tag.name không được để trống")
    private String name;
  }

  @Getter 
  @Setter 
  @NoArgsConstructor 
  @AllArgsConstructor 
  public static class InputUser{
    @NotNull(message = "User.id không được phép để trống")
    private int id;
  }
}
