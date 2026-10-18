package vn.spring.blog.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import vn.spring.blog.helper.ApiResponse;

@RestController 
public class uploadImageController {

  @Value ("${app.upload.dir}")
  String uploaddir;

  @PostMapping("api/upload")
  public ResponseEntity<ApiResponse<Object>> upload(@RequestParam("file") MultipartFile file) throws IOException {
    if(file.isEmpty()){
      return ApiResponse.error(HttpStatus.BAD_REQUEST, "file không được rỗng");
    }
    String type = file.getContentType();
    if(!"image/png".equals(type) && !"image/jpeg".equals(type)){
      return ApiResponse.error(HttpStatus.BAD_REQUEST, "Chỉ nhận file .pnj hoặc .jpg");
    }
    // tạo vào gán đường dẫn
    Path uploadPath = Path.of(uploaddir).toAbsolutePath();
    Files.createDirectories(uploadPath);

    // tạo tên khác để tránh trùng
    String extension = "image/png".equals(type) ? ".png" : ".jpg";
    String fileName = UUID.randomUUID() + extension;

    Path destination = uploadPath.resolve(fileName);
    file.transferTo(destination);
    return ApiResponse.success(null, "lưu thành công");
  } 
  
}
