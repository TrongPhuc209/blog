package vn.hoidanit.springsieutoc.model.dto.request;

import jakarta.validation.Valid;
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
public class CommentRequestDTO {

	private String content;

  @Valid 
  @NotNull (message = "Comment.user không được để trống")
	private InputUser user;

  @Valid 
  @NotNull (message = "Comment.post không được để trống")
	private InputPost post;

  @Getter 
  @Setter 
  @NoArgsConstructor 
  @AllArgsConstructor 
  @Builder 
  public static class InputUser{
    @NotNull 
    private int id;
  }

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
