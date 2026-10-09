package vn.spring.blog.helper.exception;

public class BadRequestException extends RuntimeException{
  public BadRequestException(String message){
    super(message);
  }
}
