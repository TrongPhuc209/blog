package vn.spring.blog.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.spring.blog.helper.ApiResponse;
import vn.spring.blog.helper.PageResponse;
import vn.spring.blog.model.dto.request.CommentApprovedDTO;
import vn.spring.blog.model.dto.request.CommentFilterRequestDTO;
import vn.spring.blog.model.dto.request.CommentRequestDTO;
import vn.spring.blog.model.dto.response.CommentResponseDTO;
import vn.spring.blog.service.CommentService;

@RestController 
@RequiredArgsConstructor 
public class CommentController {
  private final CommentService commentService;

  @PostMapping("/posts/{postId}/comments")
  public ResponseEntity<ApiResponse<CommentResponseDTO>> createComment(@PathVariable Long postId, @Valid @RequestBody CommentRequestDTO CommentRequestDTO){
    return ApiResponse.success(commentService.createComment(postId, CommentRequestDTO));
  }

  @GetMapping("/comments")
  public ResponseEntity<ApiResponse<PageResponse<CommentResponseDTO>>> getAllComments(CommentFilterRequestDTO comFilter, Pageable pageable){
    return ApiResponse.success(PageResponse.from(commentService.getAllComment(comFilter, pageable)));
  }

  @GetMapping("/comments/{id}")
  public ResponseEntity<ApiResponse<CommentResponseDTO>> getCommentById(@PathVariable Long id){
    return ApiResponse.success(commentService.getById(id));
  }

  @PutMapping("/comments/{id}")
  public ResponseEntity<ApiResponse<CommentResponseDTO>> updateComment(@PathVariable Long id, @Valid @RequestBody CommentRequestDTO inputComment){
    return ApiResponse.success(commentService.updateComment(inputComment, id));
  }

  @PutMapping ("/comments/{id}/approve")
  public ResponseEntity<ApiResponse<String>> approveComment(@PathVariable Long id, @RequestBody CommentApprovedDTO commentApprovedDTO){
    commentService.changeApprovedComment(id, commentApprovedDTO);
    return ApiResponse.success("Comment approved successfully");
  }

  @DeleteMapping("/comments/{id}")
  public ResponseEntity<ApiResponse<String>> deleteComment(@PathVariable Long id){
    commentService.deleteComment(id);
    return ApiResponse.success("Delete success"); 
  }
  
}
