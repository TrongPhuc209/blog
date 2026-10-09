package vn.spring.blog.model;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor 
@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@NotBlank(message = "name không được để trống")
	private String name;

	@NotBlank(message = "email không được để trống")
	private String email;

	@NotBlank(message = "password không được để trống")
	private String password;

	@NotBlank(message = "address không được để trống")
	private String address;

	private int failedLoginAttempts;

	private Instant loginLockedUntil;

	private int loginLockCount;

	@Enumerated (EnumType.STRING)
	private AccountStatus status;

	@Enumerated (EnumType.STRING)
	private AccountRestrictionReason restrictionReason;

	private String restrictionNote;

	private Instant suspendedUntil;

	@ManyToOne
	@JoinColumn(name = "role_id")
	private Role role;

	@OneToMany(mappedBy = "user")
	private List<Post> posts;

	@OneToMany(mappedBy = "user")
	private List<Comment> comments;

	@OneToMany(mappedBy = "user")
	private List<RefreshToken> refreshTokens;


	public enum AccountStatus {
		ACTIVE,
		SUSPENDED,		
	}

	public enum AccountRestrictionReason {
		TOO_MANY_LOGIN_ATTEMPTS,
		ADMIN_ACTION,
		OTHER
	}
}
