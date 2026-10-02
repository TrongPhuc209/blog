package vn.spring.blog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.spring.blog.model.RefreshToken;

import java.util.List;
import java.util.Optional;


@Repository 
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByToken(String token);
  List<RefreshToken> findAllByUserId(int userId);
}
