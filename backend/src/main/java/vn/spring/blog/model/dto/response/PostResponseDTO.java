package vn.spring.blog.model.dto.response;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
@Builder 
public class PostResponseDTO {
  private Long id;

  private Integer userId;

	private String title;

	private String content;

  private List<OutputTag> tag;

  private Instant createAt;
  
  private Instant updateAt;

  @Getter 
  @Setter 
  @NoArgsConstructor 
  @AllArgsConstructor 
  public static class OutputTag{
    private Long id;
    private String name;
  }
}
