package vn.hoidanit.springsieutoc.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.hoidanit.springsieutoc.helper.ApiResponse;
import vn.hoidanit.springsieutoc.helper.PageResponse;
import vn.hoidanit.springsieutoc.model.dto.request.CommentFilterRequestDTO;
import vn.hoidanit.springsieutoc.model.dto.request.CommentRequestDTO;
import vn.hoidanit.springsieutoc.model.dto.response.CommentResponseDTO;
import vn.hoidanit.springsieutoc.service.CommentService;

@RestController 
@RequiredArgsConstructor 
@RequestMapping("/comments")
public class CommentController {
  private final CommentService commentService;

  @PostMapping 
  public ResponseEntity<ApiResponse<CommentResponseDTO>> createComment(@Valid @RequestBody CommentRequestDTO CommentRequestDTO){
    return ApiResponse.success(commentService.createComment(CommentRequestDTO));
  }

  @GetMapping 
  public ResponseEntity<ApiResponse<PageResponse<CommentResponseDTO>>> getAllComment(CommentFilterRequestDTO comFilter, Pageable pageable){
    return ApiResponse.success(PageResponse.from(commentService.getAllComment(comFilter, pageable)));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<CommentResponseDTO>> getByCommentById(@PathVariable Long id){
    return ApiResponse.success(commentService.getById(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<CommentResponseDTO>> updateComment(@PathVariable Long id, @Valid @RequestBody CommentRequestDTO inputComment){
    return ApiResponse.success(commentService.updateComment(inputComment, id));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<String>> deleteComment(@PathVariable Long id){
    return ApiResponse.success(commentService.deleteComment(id));
  }
  
}
