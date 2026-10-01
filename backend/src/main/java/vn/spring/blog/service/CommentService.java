package vn.spring.blog.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import vn.spring.blog.helper.SecurityUtil;
import vn.spring.blog.helper.exception.ForbiddenException;
import vn.spring.blog.helper.exception.ResourceNotFoundException;
import vn.spring.blog.model.Comment;
import vn.spring.blog.model.Post;
import vn.spring.blog.model.User;
import vn.spring.blog.model.dto.request.CommentApprovedDTO;
import vn.spring.blog.model.dto.request.CommentFilterRequestDTO;
import vn.spring.blog.model.dto.request.CommentRequestDTO;
import vn.spring.blog.model.dto.response.CommentResponseDTO;
import vn.spring.blog.repository.CommentRepository;
import vn.spring.blog.repository.PostRepository;
import vn.spring.blog.repository.UserRepository;
import vn.spring.blog.service.specification.CommentSpecification;

@Service
@RequiredArgsConstructor
@Transactional 
public class CommentService {
  private final CommentRepository commentRepository;
  private final UserRepository userRepository;
  private final PostRepository postRepository;

  public CommentResponseDTO mapCommentToDto(Comment c) {
    CommentResponseDTO.OutputUser u = new CommentResponseDTO.OutputUser(c.getUser().getId(), c.getUser().getEmail(),
        c.getUser().getName());
    CommentResponseDTO.OutputPost p = new CommentResponseDTO.OutputPost(c.getPost().getId(), c.getPost().getTitle());
    return CommentResponseDTO.builder().id(c.getId()).content(c.getContent()).isApproved(c.isApproved()).user(u).post(p)
        .build();
  }

  public CommentResponseDTO createComment(Long postId, CommentRequestDTO inputComment) {
    Post currentPost = postRepository.findById(postId)
        .orElseThrow(() -> new ResourceNotFoundException("Post với id: " + postId + " không tồn tại"));

    Comment comment = new Comment();

    int loginUserId = SecurityUtil.getCurrentIdUserLogin().orElseThrow();
    User currentUser = userRepository.findById(loginUserId)
        .orElseThrow(() -> new ResourceNotFoundException("User với id: " + loginUserId + " không tồn tại"));

    comment.setContent(inputComment.getContent());
    comment.setUser(currentUser);
    comment.setPost(currentPost);
    commentRepository.save(comment);
    return mapCommentToDto(comment);
  }

  public Page<CommentResponseDTO> getAllComment(CommentFilterRequestDTO comFilter, Pageable pageable) {
    Specification<Comment> spec = Specification.allOf(CommentSpecification.hasApproved(comFilter),
        CommentSpecification.hasPostId(comFilter),
        CommentSpecification.hasUserId(comFilter));
    return commentRepository.findAll(spec, pageable).map(com -> mapCommentToDto(com));
  }

  public CommentResponseDTO getById(Long id) {
    Comment currentComment = commentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Comment với id: " + id + " không tồn tại"));
    return mapCommentToDto(currentComment);
  }

  public CommentResponseDTO updateComment(CommentRequestDTO inputComment, Long id) {
    Comment currentComment = commentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Comment với id: " + id + " không tồn tại"));

    if (!canEditComment(currentComment)) {
      throw new ForbiddenException("Bạn không có quyền sửa bình luận này");
    }
    currentComment.setContent(inputComment.getContent());
    commentRepository.save(currentComment);
    return mapCommentToDto(currentComment);
  }

  public void changeApprovedComment(Long id, CommentApprovedDTO commentApprovedDTO) {
    Comment currentComment = commentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Comment với id: " + id + " không tồn tại"));
    if ("ADMIN".equals(SecurityUtil.getCurrentRoleLogin())) {
      currentComment.setApproved(commentApprovedDTO.isApproved());
      commentRepository.save(currentComment);
    } else {
      throw new ForbiddenException("Bạn không có quyền cấp quyền bình luận này");
    }
  }

  public void deleteComment(Long id) {
    Comment currentComment = commentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Comment với id: " + id + " không tồn tại"));
    if (canDeleteComment(currentComment)) {
      commentRepository.delete(currentComment);
    } else {
      throw new ForbiddenException("Bạn không có quyền xóa bình luận này");
    }
  }

  private boolean canDeleteComment(Comment currentComment) {
    int userLogin = SecurityUtil.getCurrentIdUserLogin().orElseThrow();
    String roleLogin = SecurityUtil.getCurrentRoleLogin();
    return "ADMIN".equals(roleLogin) || currentComment.getUser().getId() == userLogin;
  }

  private boolean canEditComment(Comment currentComment) {
    int userLogin = SecurityUtil.getCurrentIdUserLogin().orElseThrow();
    return currentComment.getUser().getId() == userLogin;
  }
  
}
