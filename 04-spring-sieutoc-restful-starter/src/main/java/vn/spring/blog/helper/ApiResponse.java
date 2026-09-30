package vn.spring.blog.helper;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
public class ApiResponse<T> {
  private String status;
  private String message;
  private T data;
  private String errorCode;
  private LocalDateTime timestamp = LocalDateTime.now();


  public ApiResponse(HttpStatus status, String message, T data, String errorCode) {
    this.status = status.is2xxSuccessful() ? "success" : "error";
    this.message = message;
    this.data = data;
    this.errorCode = errorCode;
    this.timestamp = LocalDateTime.now();
  }

  // 1
  public static <T> ResponseEntity<ApiResponse<T>> success(T data){
    ApiResponse<T> response = new ApiResponse<>(HttpStatus.OK, "Call API success.", data, null);
    return ResponseEntity.ok(response);
  }
  
  // 2
  public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message){
    ApiResponse<T> response = new ApiResponse<>(HttpStatus.OK, message, data, null);
    return ResponseEntity.ok(response);
  }

  // 3
  public static <T> ResponseEntity<ApiResponse<T>> created(T data){
    ApiResponse<T> response = new ApiResponse<T>(HttpStatus.CREATED, "Created successfully.", data, null);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // 4
  public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String message, String errorCode){
    ApiResponse<T> response = new ApiResponse<T>(status, message, null, errorCode);
    return ResponseEntity.status(status).body(response);
  }

  // 5
  public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String message){
    return error(status, message, String.valueOf(status.value()));
  }

}
