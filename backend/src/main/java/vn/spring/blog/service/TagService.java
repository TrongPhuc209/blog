package vn.spring.blog.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import vn.spring.blog.helper.SecurityUtil;
import vn.spring.blog.helper.exception.ResourceAlreadyExitsException;
import vn.spring.blog.helper.exception.ResourceNotFoundException;
import vn.spring.blog.model.Post;
import vn.spring.blog.model.Tag;
import vn.spring.blog.model.dto.request.ApprovalRequestDTO;
import vn.spring.blog.model.dto.request.TagFilterRequestDTO;
import vn.spring.blog.model.dto.request.TagRequestDTO;
import vn.spring.blog.model.dto.response.PostResponseDTO;
import vn.spring.blog.model.dto.response.TagResponseDTO;
import vn.spring.blog.repository.PostRepository;
import vn.spring.blog.repository.TagRepository;
import vn.spring.blog.service.specification.TagSpecification;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {
  private final TagRepository tagRepository;
  private final PostRepository postRepository;

  private static TagResponseDTO mapTagToDto(Tag tag) {
    // Tag mới tạo chưa có post; response dùng danh sách rỗng thay vì để null.
    // Vì khi stream gọi tới null sẽ báo lỗi.
    List<Post> posts = tag.getPosts() == null ? List.of() : tag.getPosts();
    return TagResponseDTO.builder()
        .id(tag.getId())
        .name(tag.getName())
        .isApproved(tag.getIsApproved())
        .posts(posts
            .stream()
            .map(p -> TagResponseDTO.OutputPost.builder()
                .id(p.getId())
                .title(p.getTitle())
                .content(p.getContent())
                .build())
            .toList())
        .build();
  }

  public TagResponseDTO createTag(TagRequestDTO inputTag) {
    boolean isCurrentTag = tagRepository.existsByName(inputTag.getName());
    if (isCurrentTag) {
      throw new ResourceAlreadyExitsException("Tag có tên: " + inputTag.getName() + " đã tồn tại");
    }
    Tag tag = new Tag();
    tag.setName(inputTag.getName());
    tag.setIsApproved(false);
    return mapTagToDto(tagRepository.save(tag));
  }

  //admin
  public TagResponseDTO getTagById(Long id) {
    return mapTagToDto(tagRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Tag có id = %s không tồn tại" + id)));
  }

  public Page<TagResponseDTO> getAllTag(TagFilterRequestDTO tagFilter, Pageable pageable) {
    Specification<Tag> spec;

    if(!"ROLE_ADMIN".equals(SecurityUtil.getCurrentRoleLogin())){
      tagFilter.setIsApproved(true);
    }
    
    spec = Specification.allOf(TagSpecification.hasName(tagFilter),
                                TagSpecification.hasPost(tagFilter),
                                TagSpecification.hasApproved(tagFilter));
                                                
    return tagRepository.findAll(spec, pageable)
                        .map(tag -> mapTagToDto(tag));
  }

  public TagResponseDTO updateTag(Long id, TagRequestDTO inputTag) {
    Tag currentTag = tagRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Tag có id: " + id + " không tồn tại"));

    boolean currentCheck = tagRepository.existsByName(inputTag.getName());
    if (currentCheck) {
      throw new ResourceAlreadyExitsException("Tag có tên: " + inputTag.getName() + " đã tồn tại");
    }
    currentTag.setName(inputTag.getName());
    return mapTagToDto(tagRepository.save(currentTag));
  }

  //admin
  public void modifyTheApproval(Long tagID, ApprovalRequestDTO tagApprovalDTO){
    Tag currentTag = tagRepository.findById(tagID).orElseThrow(() -> new ResourceNotFoundException("Tag không tồn tại"));

    currentTag.setIsApproved(tagApprovalDTO.getIsApproved());
    tagRepository.save(currentTag);
  }

  public List<PostResponseDTO> deleteTag(Long id){
    Tag currentTag = tagRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Id tag không tồn tại"));
    List<Post> listPost = postRepository.findByTagsContains(currentTag);
    return listPost.stream()
        .map(post -> PostResponseDTO.builder()
            .id(post.getId())
            .title(post.getTitle())
            .content(post.getContent())
            .build())
        .toList();
  }

  public void comfirmDeleteTag(long id) {
    Tag currentTag = tagRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Id tag không tồn tại"));

    List<Post> listPost = postRepository.findByTagsContains(currentTag);
    for (Post post : listPost) {
      post.getTags().remove(currentTag);
      postRepository.save(post);
    }
    tagRepository.deleteById(id);
  }

}
