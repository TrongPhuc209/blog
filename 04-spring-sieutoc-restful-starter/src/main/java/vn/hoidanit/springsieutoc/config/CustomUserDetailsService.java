package vn.hoidanit.springsieutoc.config;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import lombok.RequiredArgsConstructor;
import vn.hoidanit.springsieutoc.service.UserService;
 
@RequiredArgsConstructor 
public class CustomUserDetailsService implements UserDetailsService {
  private final UserService userService;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
    vn.hoidanit.springsieutoc.model.User myUser =  this.userService.findUserByEmai(username);
    if(myUser == null){
      throw new UsernameNotFoundException("User not found");
    }
    return new User(myUser.getEmail(), myUser.getPassword(), Collections.singleton(new SimpleGrantedAuthority("ROLE_" + myUser.getRole().getName())));
  }
}
  