package vn.hoidanit.springsieutoc.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import vn.hoidanit.springsieutoc.helper.exception.ResourceNotFoundException;
import vn.hoidanit.springsieutoc.model.Post;
import vn.hoidanit.springsieutoc.model.Tag;
import vn.hoidanit.springsieutoc.model.User;
import vn.hoidanit.springsieutoc.model.dto.request.PostFilterRequestDTO;
import vn.hoidanit.springsieutoc.model.dto.request.PostRequestDTO;
import vn.hoidanit.springsieutoc.model.dto.response.PostResponseDTO;
import vn.hoidanit.springsieutoc.repository.PostRepository;
import vn.hoidanit.springsieutoc.repository.TagRepository;
import vn.hoidanit.springsieutoc.repository.UserRepository;
import vn.hoidanit.springsieutoc.service.specification.PostSpecification;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {
  private final PostRepository postRepository;
  private final TagRepository tagRepository;
  private final UserRepository userRepository;

  public PostResponseDTO convertPostToDTO(Post post) {
    return PostResponseDTO.builder()
        .id(post.getId())
        .title(post.getTitle())
        .content(post.getContent())
        .tag(post.getTags() != null ? post.getTags().stream()
            .map(tag -> new PostResponseDTO.OutputTag(tag.getId(), tag.getName()))
            .collect(Collectors.toList()) : null)
        .createAt(post.getCreatedAt())
        .updateAt(post.getUpdatedAt())
        .build();
  }

  public Post convertDtoToPost(PostRequestDTO dto, Post p) {
    p.setTitle(dto.getTitle());
    p.setContent(dto.getContent());
    if (dto.getTag() != null) {
      List<Tag> tags = dto.getTag()
          .stream()
          .map(tag -> tagRepository.findById(tag.getId())
              .orElseThrow(() -> new ResourceNotFoundException("Tag có id: " + tag.getId() + " không tồn tại")))
          .collect(Collectors.toList());
      p.setTags(tags);
    }
    if (dto.getUser() != null) {
      User u = userRepository.findById(dto.getUser().getId())
          .orElseThrow(() -> new ResourceNotFoundException("User có id: " + dto.getUser().getId() + " không tồn tại!"));
      p.setUser(u);
    }
    return p;
  }

  public PostResponseDTO createPost(PostRequestDTO inputPost) {
    Post p = convertDtoToPost(inputPost, new Post());
    postRepository.save(p);
    return convertPostToDTO(p);
  }

  public Page<PostResponseDTO> getAllPost(PostFilterRequestDTO postFilter, Pageable pageable) {
    Specification<Post> spec = Specification.allOf(PostSpecification.hasTitle(postFilter),
                                                  PostSpecification.hasContent(postFilter),
                                                  PostSpecification.hasUserId(postFilter),
                                                  PostSpecification.hasTagName(postFilter),
                                                  PostSpecification.createAtFromTo(postFilter));
    return postRepository.findAll(spec, pageable).map(p -> convertPostToDTO(p));
  }

  public PostResponseDTO getPostById(Long id) {
    return convertPostToDTO(postRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Post với id: " + id + " không tồn tại")));
  }

  public PostResponseDTO updatePost(PostRequestDTO inputPost, Long id) {
    Post currentPost = postRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Post với id: " + id + " không tồn tại"));

    currentPost = convertDtoToPost(inputPost, currentPost);

    postRepository.save(currentPost);
    return convertPostToDTO(currentPost);
  }

  public String deletePost(Long id) {
    boolean check = postRepository.existsById(id);
    if (check) {
      postRepository.deleteById(id);
      return "Delete success";
    }
    return "Detele fail";
  }

}
