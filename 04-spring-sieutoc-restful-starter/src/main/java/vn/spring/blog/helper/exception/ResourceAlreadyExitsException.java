package vn.spring.blog.helper.exception;

public class ResourceAlreadyExitsException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public ResourceAlreadyExitsException(String message){
    super(message);
  }
}
