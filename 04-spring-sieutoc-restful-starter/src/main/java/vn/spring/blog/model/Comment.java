package vn.spring.blog.model;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "comments")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
public class Comment {
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank (message = "Content không được để trống")
	private String content;

	private boolean isApproved;

	private Instant createdAt;

	private Instant updatedAt;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne
	@JoinColumn(name = "post_id")
	private Post post;

	@PrePersist 
	public void createComment(){
		this.createdAt = Instant.now();
		this.updatedAt = Instant.now();
	}

	@PreUpdate 
	public void updateComment(){
		this.updatedAt = Instant.now();
	}

	public Comment(String content, User user, Post post){
		this.content = content;
		this.user = user;
		this.post = post;
	}
}
