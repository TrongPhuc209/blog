package vn.spring.blog.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.spring.blog.helper.exception.ResourceNotFoundException;
import vn.spring.blog.model.RefreshToken;
import vn.spring.blog.repository.RefreshTokenRepository;

@Service
@RequiredArgsConstructor 
public class RefreshTokenService {
  private final RefreshTokenRepository refreshTokenRepository;

  public void saveRefreshToken(RefreshToken refreshToken){
    refreshTokenRepository.save(refreshToken);
  }

  public RefreshToken findByToken(String token){
    RefreshToken currentToken = this.refreshTokenRepository.findByToken(token).orElseThrow(() -> new ResourceNotFoundException("Token không tồn tại"));
    return currentToken;
  }

  public void deleteById(Long id){
    this.refreshTokenRepository.deleteById(id);
  }
}
