package vn.spring.blog.model.dto.response;

import java.time.Instant;

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
public class CommentResponseDTO { 

  private Long id;

	private String content;

	private boolean isApproved;

  private int replyCount;

  private Instant createAt;

  private Instant updateAt;
  
  private Instant editedAt;

	private OutputUser user;

	private OutputPost post;

  @Getter 
  @Setter 
  @NoArgsConstructor 
  @AllArgsConstructor 
  @Builder 
  public static class OutputUser{
    private int id;
    private String email;
    private String name;
  }

  @Getter 
  @Setter 
  @NoArgsConstructor 
  @AllArgsConstructor 
  @Builder 
  public static class OutputPost{
    private Long id;
    private String title;
  }
}
