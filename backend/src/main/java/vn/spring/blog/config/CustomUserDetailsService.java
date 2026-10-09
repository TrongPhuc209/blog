package vn.spring.blog.config;

import java.time.Instant;
import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import lombok.RequiredArgsConstructor;
import vn.spring.blog.helper.exception.UnauthorizedException;
import vn.spring.blog.model.User;
import vn.spring.blog.model.User.AccountStatus;
import vn.spring.blog.repository.UserRepository;
import vn.spring.blog.service.UserService;

@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
  private final UserService userService;
  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    vn.spring.blog.model.User myUser = this.userService.findUserByEmai(username);
    if (myUser == null) {
      throw new UsernameNotFoundException("User not found");
    }
    

    return org.springframework.security.core.userdetails.User
        .withUsername(myUser.getEmail())
        .password(myUser.getPassword())
        .authorities(Collections.singleton(new SimpleGrantedAuthority("ROLE_" + myUser.getRole().getName())))
        .accountLocked(checkLogin(myUser))
        .build();
    // return new User(myUser.getEmail(), myUser.getPassword(),
    // Collections.singleton(new SimpleGrantedAuthority("ROLE_" +
    // myUser.getRole().getName())));
  }

  public boolean checkLogin(User user){

    if(AccountStatus.SUSPENDED.equals(user.getStatus())){
      if(user.getSuspendedUntil() == null){
        throw new UnauthorizedException("Bạn đã bị cấm do: " + user.getRestrictionNote());
      }
      
      if(user.getSuspendedUntil().isAfter(Instant.now())){
        throw new UnauthorizedException("Bạn đang bị cấm do: " + user.getRestrictionNote());
      }

      user.setStatus(AccountStatus.ACTIVE);
      user.setSuspendedUntil(null);
      user.setRestrictionNote(null);
      user. setRestrictionReason(null);

      userRepository.save(user);
    }

    boolean checkLockedUntil = user.getLoginLockedUntil() != null ? user.getLoginLockedUntil().isAfter(Instant.now()) : false;

    return checkLockedUntil;
  }
}
