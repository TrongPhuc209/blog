package vn.spring.blog.service;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.spring.blog.model.Comment;
import vn.spring.blog.model.Post;
import vn.spring.blog.model.Role;
import vn.spring.blog.model.Tag;
import vn.spring.blog.model.User;
import vn.spring.blog.repository.CommentRepository;
import vn.spring.blog.repository.PostRepository;
import vn.spring.blog.repository.RoleRepository;
import vn.spring.blog.repository.TagRepository;
import vn.spring.blog.repository.UserRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final TagRepository tagRepository;
	private final PostRepository postRepository;
	private final CommentRepository commentRepository;

	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public void run(String... args) throws Exception {
		long userCount = this.userRepository.count();

		if (userCount > 0) {
			log.info("--- SKIP DATA SEEDER ---");
			return;
		}
		log.info("--- START DATA SEEDER ---");

		// create roles
		Role adminRole = new Role(null, "ADMIN", "ADMIN full quyền", null);
		Role userRole = new Role(null, "USER", "Normal user", null);

		adminRole = this.roleRepository.save(adminRole);
		userRole = this.roleRepository.save(userRole);
		String defaultPassword = this.passwordEncoder.encode("333333");

		// create users
		User adminUser = new User();
		adminUser.setEmail("admin@gmail.com");
		adminUser.setName("Trong Phuc");
		adminUser.setAddress("vietnam");
		adminUser.setRole(adminRole);
		adminUser.setPassword(defaultPassword);
		adminUser = this.userRepository.save(adminUser);

		User normalUser = new User();
		normalUser.setEmail("user@gmail.com");
		normalUser.setName("@TrongPhuc");
		normalUser.setAddress("vietnam");
		normalUser.setRole(userRole);
		normalUser.setPassword(defaultPassword);
		normalUser = this.userRepository.save(normalUser);

		// create tags
		List<String> TAGS = List.of("JAVA", "SPRING", "SPRING BOOT", "HIBERNATE", "JPA",
				"JAVASCRIPT", "TYPESCRIPT", "NODEJS", "EXPRESSJS", "REACTJS", "ANGULAR",
				"VUEJS", "NEXTJS", "NUXTJS", "HTML", "CSS", "TAILWIND CSS", "BOOTSTRAP",
				"MYSQL", "POSTGRESQL", "MONGODB", "REDIS", "DOCKER", "KUBERNETES", "AWS",
				"GCP", "AZURE", "DEVOPS", "MICROSERVICES", "SYSTEM DESIGN");
		for (String tagName : TAGS) {
			Tag t = new Tag();
			t.setName(tagName);
			this.tagRepository.save(t);
		}

		// create posts

		List<Post> POSTS = List.of(
				new Post("Giới thiệu Spring Boot cho người mới bắt đầu",
						"""
								Spring Boot là một framework giúp bạn xây dựng ứng dụng Java nhanh chóng và hiệu quả.

								Trong bài viết này, chúng ta sẽ tìm hiểu:
								- Spring Boot là gì?
								- Vì sao nên dùng Spring Boot?
								- Cách tạo project Spring Boot đầu tiên

								Bài viết phù hợp cho người mới học Java Web.
								""",
						adminUser),
				new Post("REST API là gì? Cách thiết kế RESTful API chuẩn", """
						REST API là nền tảng của hầu hết các hệ thống backend hiện đại.

						Một RESTful API tốt cần:
						- URL rõ ràng
						- Dùng đúng HTTP method
						- Trả về HTTP status code phù hợp

						Bài viết này sẽ giúp bạn hiểu rõ từ cơ bản đến thực hành.
						""", adminUser),
				new Post("JWT Authentication trong Spring Boot",
						"""
								JWT (JSON Web Token) là giải pháp phổ biến cho xác thực và phân quyền.

								Nội dung bài viết:
								- JWT là gì?
								- Access Token và Refresh Token
								- Flow đăng nhập với JWT trong Spring Boot

								Kèm ví dụ thực tế.
								""",
						adminUser),
				new Post("Docker cho lập trình viên backend",
						"""
								Docker giúp bạn đóng gói ứng dụng và môi trường chạy thành container.

								Bài viết này sẽ hướng dẫn:
								- Docker là gì?
								- Viết Dockerfile cho Spring Boot
								- Chạy MySQL bằng Docker Compose
								""",
						adminUser),
				new Post("Tối ưu hiệu năng ứng dụng Spring Boot", """
						Khi ứng dụng lớn dần, hiệu năng là yếu tố rất quan trọng.

						Một số kỹ thuật tối ưu:
						- Connection Pool (HikariCP)
						- Cache với Redis
						- Tối ưu query JPA

						Phù hợp cho ứng dụng production.
						""", adminUser));

		for (Post post : POSTS) {
			this.postRepository.save(post);
		}

		// create comments
		List<Post> posts = this.postRepository.findAll();
		if (posts.isEmpty())
			return;

		for (Post post : posts) {
			List<String> comments = List.of("Bài viết rất hay và dễ hiểu 👍",
					"Cảm ơn tác giả đã chia sẻ!", "Mình đã áp dụng và thấy rất hiệu quả.",
					"Có thể viết thêm ví dụ chi tiết hơn không?",
					"Phần này mình chưa hiểu lắm, mong được giải thích thêm.");

			for (String content : comments) {
				Comment c = new Comment(content, normalUser, post);
				this.commentRepository.save(c);
			}
		}

		log.info("--- FINISH DATA SEEDER ---");
	}

}