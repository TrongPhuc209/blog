package vn.spring.blog.model.dto.request;

import java.time.Instant;
import java.util.List;

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
public class PostFilterRequestDTO {
  // lọc theo field thuộc Post
  private String title;
  private String content;
  private List<String> tagName;
  private Instant from;
  private Instant to;
  
  // lọc theo field thuộc User
  private Integer userId;
  private String userName;
}
