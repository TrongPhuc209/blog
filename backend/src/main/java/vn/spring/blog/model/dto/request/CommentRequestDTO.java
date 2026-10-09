package vn.spring.blog.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
public class CommentRequestDTO {

  @NotBlank (message = "Content không được để trống")
	private String content;

  @Valid 
  @NotNull (message = "Comment.post không được để trống")
	private InputPost post;

  private Long parentId;

  @Getter 
  @Setter 
  @NoArgsConstructor 
  @AllArgsConstructor 
  @Builder 
  public static class InputPost{
    @NotNull 
    private Long id;
  }
}
