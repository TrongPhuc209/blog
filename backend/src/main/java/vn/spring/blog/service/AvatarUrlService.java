package vn.spring.blog.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import vn.spring.blog.model.MediaAsset;
import vn.spring.blog.model.User;

@Service
public class AvatarUrlService {
  private final String publicBaseUrl;

  public AvatarUrlService(@Value("${app.storage.r2.public-base-url}") String publicBaseUrl) {
    this.publicBaseUrl = publicBaseUrl.trim().replaceAll("/+$", "");
    if (this.publicBaseUrl.isBlank()) {
      throw new IllegalStateException("Thiếu R2_PUBLIC_BASE_URL");
    }
  }

  public String getAvatarUrl(User user) {
    if (user == null) return null;
    MediaAsset avatar = user.getAvatar();
    if (avatar == null || avatar.getStorageKey() == null || avatar.getStorageKey().isBlank()) {
      return null;
    }

    String storageKey = avatar.getStorageKey().replaceAll("^/+", "");
    return publicBaseUrl + "/" + storageKey;
  }
}
