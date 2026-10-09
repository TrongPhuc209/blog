package vn.spring.blog.model.dto.request;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.spring.blog.model.User.AccountRestrictionReason;
import vn.spring.blog.model.User.AccountStatus;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SuspendedAccountRequestDTO {
  private AccountStatus status;
  private AccountRestrictionReason reason;
  private String reasonDetails;
  private Instant suspendedUntil;
}
