package vn.hoidanit.springsieutoc.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter 
@Setter 
@AllArgsConstructor
@NoArgsConstructor 
@ToString 
public class UserFilterRequestDTO {
  private String name;
  private String address;
  private String email;
  private String role;
}
