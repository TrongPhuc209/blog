package vn.spring.blog.helper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.persistence.EntityNotFoundException;
import vn.spring.blog.helper.exception.ResourceAlreadyExitsException;
import vn.spring.blog.helper.exception.ResourceNotFoundException;

@RestControllerAdvice 
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handlerAllException(Exception ex){
    return ApiResponse.error(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<?> handleNotFound(EntityNotFoundException ex){
    return ApiResponse.error(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<?> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex){
    String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", 
                                    ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());
    return ApiResponse.error(HttpStatus.BAD_REQUEST, message);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> methodArgumentNotValidException(MethodArgumentNotValidException ex){
    List<String> errorList = ex.getBindingResult().getFieldErrors().stream()
    .map(error -> error.getField() + ": " + error.getDefaultMessage())
    .collect(Collectors.toList());
    
    String error = String.join("; ", errorList);

    // ApiResponse<Object> response = new ApiResponse<>(HttpStatus.BAD_REQUEST, error, null, String.valueOf(HttpStatus.BAD_REQUEST.value()));
    // return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    return ApiResponse.error(HttpStatus.BAD_REQUEST, error);
  }

  @ExceptionHandler({ResourceAlreadyExitsException.class, ResourceNotFoundException.class })
  public ResponseEntity<?> resourseException(Exception ex){
    return ApiResponse.error(HttpStatus.BAD_REQUEST, ex.getMessage());
  }
  
}
