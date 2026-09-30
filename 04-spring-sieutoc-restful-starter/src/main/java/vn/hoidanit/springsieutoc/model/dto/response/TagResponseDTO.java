package vn.hoidanit.springsieutoc.model.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
public class TagResponseDTO {
  private Long id;
  private String name;
  private List<OutputPost> posts;

  @Getter 
  @Setter 
  @NoArgsConstructor 
  @AllArgsConstructor 
  @Builder 
  public static class OutputPost{
    private Long id;
    private String title;
    private String content;
  }
}
