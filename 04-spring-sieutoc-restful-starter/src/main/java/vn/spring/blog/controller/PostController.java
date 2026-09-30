package vn.spring.blog.controller;

import java.util.List;

import org.springframework.data.domain.Page;
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
import vn.spring.blog.helper.ApiResponse;
import vn.spring.blog.helper.PageResponse;
import vn.spring.blog.model.dto.request.PostFilterRequestDTO;
import vn.spring.blog.model.dto.request.PostRequestDTO;
import vn.spring.blog.model.dto.response.PostResponseDTO;
import vn.spring.blog.service.PostService;

@RestController 
@RequestMapping("/posts")
@RequiredArgsConstructor 
public class PostController {
  private final PostService postService;

  @PostMapping 
  public ResponseEntity<ApiResponse<PostResponseDTO>> createPost(@Valid @RequestBody PostRequestDTO inputPost){
    return ApiResponse.success(postService.createPost(inputPost));
  }

  @GetMapping 
  public ResponseEntity<ApiResponse<PageResponse<PostResponseDTO>>> getAllPost(PostFilterRequestDTO postFilter,Pageable pageable){
    return ApiResponse.success(PageResponse.from(postService.getAllPost(postFilter, pageable))); 
  }

  @GetMapping ("/{id}")
  public ResponseEntity<ApiResponse<PostResponseDTO>> getById(@PathVariable Long id){
    return ApiResponse.success(postService.getPostById(id));
  }

  @PutMapping ("/{id}")
  public ResponseEntity<ApiResponse<PostResponseDTO>> updatePost(@Valid @RequestBody PostRequestDTO inputPost, @PathVariable Long id){
    return ApiResponse.success(postService.updatePost(inputPost, id));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<String>> detelePost(@PathVariable Long id){
    return ApiResponse.success(postService.deletePost(id));
  }
}
