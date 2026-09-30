package vn.hoidanit.springsieutoc.model.dto.request;

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
  private String title;
  private String content;
  private String userId;
  private List<String> tagName;
  private Instant from;
  private Instant to;
}
