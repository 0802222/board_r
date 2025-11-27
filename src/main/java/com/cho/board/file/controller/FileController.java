package com.cho.board.file.controller;

import com.cho.board.file.service.FileService;
import com.cho.board.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    // Post 작성 전 파일 먼저 업로드
    // editor 에서 drag & drop 지원
    @PostMapping("/temp")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadTempFile(
        @RequestParam("file") MultipartFile file) {

        String filename = fileService.uploadPostImage(file);

        Map<String, String> data = new HashMap<>();
        data.put("filename", filename);
        data.put("url", "/uploads/posts/" + filename);

        return ResponseEntity.ok(ApiResponse.success(
            data,
            "파일이 업로드되었습니다."));
    }
}
