package org.zerock.uploadapi.controller;


import lombok.Cleanup;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnailator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.uploadapi.exception.UploadException;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/apifile")
@Log4j2
public class UploadController {

    @Value("${org.zerock.upload}")
    private String uploadFolder;

    @PostMapping("upload")
    public ResponseEntity<List<String>> uploadFiles (MultipartFile[] files) {


        if(files == null || files.length == 0){
            return ResponseEntity.noContent().build();
        }


        List<String> uploadedFiles = new ArrayList<>();

        for(MultipartFile file : files){

            String fileName = UUID.randomUUID().toString()+"_"+ file.getOriginalFilename();

            try {

                File savedFile = new File(uploadFolder, fileName);
                FileCopyUtils.copy(file.getBytes(), savedFile);

                if(file.getContentType().startsWith("image")){

                    String thumbnailFileName = "s_" + fileName;

                    @Cleanup
                    InputStream inputStream = new FileInputStream(new File(uploadFolder, fileName));
                    @Cleanup
                    OutputStream outputStream = new FileOutputStream(new File(uploadFolder, thumbnailFileName));

                    Thumbnailator.createThumbnail(inputStream, outputStream, 200, 200);
                }

                uploadedFiles.add(fileName);

            } catch (IOException e) {

                e.printStackTrace();
                throw new UploadException(e.getMessage());
            }


        }

        return ResponseEntity.ok(uploadedFiles);
    }


    @GetMapping("/view/{filename}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            // 파일 경로 로드

            Path filePath = Paths.get(uploadFolder, filename);

            // 파일을 Resource로 변환
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            // 파일 MIME 타입 감지
            String contentType = Files.probeContentType(filePath);

            // 기본 Content-Type 설정
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // Content-Disposition 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            if (isImage(contentType)) {
                // 이미지 파일은 브라우저에서 바로 렌더링 (헤더 추가 없음)
                headers.setContentType(MediaType.parseMediaType(contentType));
            } else {
                // 그 외 파일은 다운로드로 처리
                headers.setContentType(MediaType.parseMediaType(contentType));
                headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"");
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // 이미지 파일 여부 확인
    private boolean isImage(String contentType) {
        return contentType.startsWith("image/");
    }

}
