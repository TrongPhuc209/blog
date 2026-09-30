package vn.hoidanit.springsieutoc.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.hoidanit.springsieutoc.helper.exception.ResourceNotFoundException;
import vn.hoidanit.springsieutoc.model.RefreshToken;
import vn.hoidanit.springsieutoc.repository.RefreshTokenRepository;

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
