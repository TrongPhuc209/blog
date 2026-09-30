package vn.hoidanit.springsieutoc.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import vn.hoidanit.springsieutoc.helper.exception.ResourceAlreadyExitsException;
import vn.hoidanit.springsieutoc.helper.exception.ResourceNotFoundException;
import vn.hoidanit.springsieutoc.model.Post;
import vn.hoidanit.springsieutoc.model.Tag;
import vn.hoidanit.springsieutoc.model.dto.request.TagFilterRequestDTO;
import vn.hoidanit.springsieutoc.model.dto.response.TagResponseDTO;
import vn.hoidanit.springsieutoc.repository.PostRepository;
import vn.hoidanit.springsieutoc.repository.TagRepository;
import vn.hoidanit.springsieutoc.service.specification.TagSpecification;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {
  private final TagRepository tagRepository;
  private final PostRepository postRepository;

  private static TagResponseDTO mapTagToDto(Tag tag) {
    return TagResponseDTO.builder()
        .id(tag.getId())
        .name(tag.getName())
        .posts(tag.getPosts()
            .stream()
            .map(p -> TagResponseDTO.OutputPost.builder()
                .id(p.getId())
                .title(p.getTitle())
                .content(p.getContent())
                .build())
            .toList())
        .build();
  }

  public Tag createTag(Tag tag) {
    boolean isCurrentTag = tagRepository.existsByName(tag.getName());
    if (isCurrentTag == true) {
      throw new ResourceAlreadyExitsException("Tag có tên: " + tag.getName() + " đã tồn tại");
    }
    return tagRepository.save(tag);
  }

  public Tag getTagById(Long id) {
    return tagRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Tag có id = %s không tồn tại" + id));
  }

  public Page<TagResponseDTO> getAllTag(TagFilterRequestDTO tagFilter, Pageable pageable) {
    Specification<Tag> spec = Specification.allOf(TagSpecification.hasName(tagFilter),
                                                  TagSpecification.hasPost(tagFilter));
    return tagRepository.findAll(spec, pageable)
                        .map(tag -> mapTagToDto(tag));
  }

  public Tag updateTag(Tag tag) {
    Tag currentTag = getTagById(tag.getId());
    boolean currentCheck = tagRepository.existsByName(tag.getName());
    if (currentCheck) {
      throw new ResourceAlreadyExitsException("Tag có tên: " + tag.getName() + " đã tồn tại");
    }
    currentTag.setName(tag.getName());
    return tagRepository.save(currentTag);
  }

  public void dateleTag(long id) {
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
