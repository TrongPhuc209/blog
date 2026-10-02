package vn.spring.blog.controller;

import java.util.List;

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
import vn.spring.blog.model.Tag;
import vn.spring.blog.model.dto.request.TagFilterRequestDTO;
import vn.spring.blog.model.dto.request.TagRequestDTO;
import vn.spring.blog.model.dto.response.PostResponseDTO;
import vn.spring.blog.model.dto.response.TagResponseDTO;
import vn.spring.blog.service.TagService;

@RequestMapping("/tags")
@RestController
@RequiredArgsConstructor 
public class TagController {
  private final TagService tagService;

  @PostMapping
  public ResponseEntity<ApiResponse<TagResponseDTO>> createTag(@Valid @RequestBody TagRequestDTO inputTag){
    return ApiResponse.created(tagService.createTag(inputTag));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<TagResponseDTO>> getById(@PathVariable Long id){
    return ApiResponse.success(tagService.getTagById(id));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<PageResponse<TagResponseDTO>>> getAllTag(TagFilterRequestDTO tagFilter, Pageable pageble){
    return ApiResponse.success(PageResponse.from(tagService.getAllTag(tagFilter, pageble)));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<TagResponseDTO>> updateTag(@PathVariable Long id, @Valid @RequestBody TagRequestDTO inputTag){
    return ApiResponse.success(tagService.updateTag(id, inputTag));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<List<PostResponseDTO>>> deleteTag(@PathVariable Long id){
    
    return ApiResponse.success(tagService.deleteTag(id), "List post đang sử dụng tag này");
  }

  @DeleteMapping("/confirm-delete/{id}")
  public ResponseEntity<ApiResponse<String>> comfirmDeleteTag(@PathVariable Long id){
    tagService.comfirmDeleteTag(id);
    return ApiResponse.success("Xóa thành công");
  }
}
