package vn.hoidanit.springsieutoc.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.hoidanit.springsieutoc.helper.exception.ResourceNotFoundException;
import vn.hoidanit.springsieutoc.model.Comment;
import vn.hoidanit.springsieutoc.model.Post;
import vn.hoidanit.springsieutoc.model.User;
import vn.hoidanit.springsieutoc.model.dto.request.CommentFilterRequestDTO;
import vn.hoidanit.springsieutoc.model.dto.request.CommentRequestDTO;
import vn.hoidanit.springsieutoc.model.dto.response.CommentResponseDTO;
import vn.hoidanit.springsieutoc.repository.CommentRepository;
import vn.hoidanit.springsieutoc.repository.PostRepository;
import vn.hoidanit.springsieutoc.repository.UserRepository;
import vn.hoidanit.springsieutoc.service.specification.CommentSpecification;

@Service
@RequiredArgsConstructor
public class CommentService {
  private final CommentRepository commentRepository;
  private final UserRepository userRepository;
  private final PostRepository postRepository;

  public Comment mapDtoToComment(CommentRequestDTO inputComment, Comment c) {
    c.setContent(inputComment.getContent());
    c.setApproved(true);
    User currentUser = userRepository.findById(inputComment.getUser().getId()).orElseThrow(
        () -> new ResourceNotFoundException("User với id: " + inputComment.getUser().getId() + " không tồn tại"));
    Post currentPost = postRepository.findById(inputComment.getPost().getId()).orElseThrow(
        () -> new ResourceNotFoundException("Post với id: " + inputComment.getPost().getId() + " không tồn tại"));
    c.setUser(currentUser);
    c.setPost(currentPost);
    return c;
  }

  public CommentResponseDTO mapCommentToDto(Comment c) {
    CommentResponseDTO.OutputUser u = new CommentResponseDTO.OutputUser(c.getUser().getId(), c.getUser().getEmail(),
        c.getUser().getName());
    CommentResponseDTO.OutputPost p = new CommentResponseDTO.OutputPost(c.getPost().getId(), c.getPost().getTitle());
    return CommentResponseDTO.builder().id(c.getId()).content(c.getContent()).isApproved(c.isApproved()).user(u).post(p).build();
  }

  public CommentResponseDTO createComment(CommentRequestDTO inputComment) {
    Comment comment = mapDtoToComment(inputComment, new Comment());
    comment.setCreatedAt(Instant.now());
    commentRepository.save(comment);
    return mapCommentToDto(comment);
  }

  public Page<CommentResponseDTO> getAllComment(CommentFilterRequestDTO comFilter, Pageable pageable){
    Specification<Comment> spec = Specification.allOf(CommentSpecification.hasApproved(comFilter),
                                                      CommentSpecification.hasPostId(comFilter),
                                                      CommentSpecification.hasUserId(comFilter));
    return commentRepository.findAll(spec, pageable).map(com -> mapCommentToDto(com));
  }

  public CommentResponseDTO getById(Long id){
    Comment currentComment = commentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Comment với id: " + id + " không tồn tại"));
    return mapCommentToDto(currentComment);
  }

  public CommentResponseDTO updateComment(CommentRequestDTO inputComment, Long id){
    Comment currentComment = commentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Comment với id: " + id + " không tồn tại"));
    
    mapDtoToComment(inputComment, currentComment);
    currentComment.setUpdatedAt(Instant.now());
    commentRepository.save(currentComment);
    return mapCommentToDto(currentComment);
  }

  public String deleteComment(Long id){
    boolean checkCommentExist = commentRepository.existsById(id);
    if(checkCommentExist){
      commentRepository.deleteById(id);
      return "delete success";
    }
    return "delete fail";
  }
}
