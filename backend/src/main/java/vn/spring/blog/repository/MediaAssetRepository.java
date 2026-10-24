package vn.spring.blog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.spring.blog.model.MediaAsset;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
}
