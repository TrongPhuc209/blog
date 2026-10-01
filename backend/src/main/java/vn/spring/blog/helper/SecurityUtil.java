package vn.spring.blog.helper;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

public class SecurityUtil {
  private static Authentication getAuthentication(){
    return SecurityContextHolder.getContext().getAuthentication();
  }

  public static Optional<String> getCurrentUsernameLogin(){
    Authentication authentication = getAuthentication();
    return Optional.ofNullable(extractUsername(authentication));
  }

  public static Optional<Integer> getCurrentIdUserLogin(){
    Authentication authentication = getAuthentication();
    return extractUserIdLogin(authentication);
  }

  public static String getCurrentRoleLogin(){
    Authentication authentication = getAuthentication();
    return extractRole(authentication);
  }

  private static String extractUsername(Authentication authentication){
    if(authentication == null){
      return null;
    }
    Object principal = authentication.getPrincipal();
    if(principal instanceof Jwt jwt){
      return jwt.getSubject();
    }
    if(principal instanceof UserDetails springSecurityUser){
      return springSecurityUser.getUsername();
    }
    if(principal instanceof String s){
      return s;
    }
    return null;
  }

  private static Optional<Integer> extractUserIdLogin(Authentication authentication){
    if(authentication == null){
      return null;
    }
    Object principal = authentication.getPrincipal();
    if(principal instanceof Jwt jwt){
      String claimId = jwt.getClaimAsString("id");
      if(claimId == null){
        return null;
      }
      try{
        return Optional.of(Integer.parseInt(claimId));
      } catch(NumberFormatException ex){
        return Optional.empty();
      }
    }
    return Optional.empty();
  }

  private static String extractRole(Authentication authentication){
    if(authentication == null){
      return null;
    }
    Object principal = authentication.getPrincipal();
    if(principal instanceof Jwt jwt){
      String claimRole = jwt.getClaimAsString("role");
      if(claimRole == null){
        return null;
      }
      return claimRole;
    }
    return null;
  }
}
