package vn.spring.blog.model;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "posts")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor
public class Post {
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Title không được để trống")
	private String title;

	@NotBlank(message = "Content không được để trống")
	@Column(columnDefinition = "MEDIUMTEXT")
	private String content;

	private Instant createdAt;
	
	private Instant updatedAt;

	@ManyToOne
	@JoinColumn (name = "user_id")
	private User user;

	@OneToMany(mappedBy = "post")
	private List<Comment> comments;

	@ManyToMany
	@JoinTable(
		name = "post_tag",
		joinColumns = @JoinColumn(name = "post_id"),
		inverseJoinColumns = @JoinColumn(name = "tag_id")
	)
	private List<Tag> tags;

	@PrePersist 
	private void beforeCreate(){
		this.createdAt = Instant.now();
		this.updatedAt = Instant.now();

		// trường hợp dòng này sẽ làm sập chương trình là khi tạo post bằng code ở server khi chưa login
		// User u = new User();
		// int userId = SecurityUtil.getCurrentIdUserLogin().get();
		// u.setId(userId);
		// this.setUser(u);
	}

	@PreUpdate 
	private void beforeUpdate(){
		this.updatedAt = Instant.now();
	}

	public Post(String title, String content, User user){
		this.title = title;
		this.content = content;
		this.user = user;
	}
}
