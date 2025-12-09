package com.cho.board.file.controller;

import com.cho.board.file.service.FileService;
import com.cho.board.global.response.ApiResponse;
import com.cho.board.global.util.FileStorageUtil;
import com.cho.board.post.dtos.PostImageResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final FileStorageUtil fileStorageUtil;

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

    // 게시글 이미지 업로드
    @PostMapping("posts/{postId}/images")
    public ResponseEntity<ApiResponse<List<PostImageResponse>>> uploadPostImages(
        @PathVariable Long postId,
        @RequestParam("files") List<MultipartFile> files) {

        List<PostImageResponse> responses = fileService.uploadPostImages(postId, files);
        return ResponseEntity.ok(ApiResponse.success(responses,
            responses.size() + "개의 이미지가 업로드되었습니다."));
    }

    // 게시글 이미지 삭제
    @DeleteMapping("/posts/{postId}/images/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deletePostImage(
        @PathVariable Long postId,
        @PathVariable Long imageId) {

        fileService.deletePostImage(postId, imageId);
        return ResponseEntity.ok(ApiResponse.success(null, "이미지가 삭제되었습니다."));
    }

    // 게시글 이미지 목록 조회
    @GetMapping("/posts/{postId}/images")
    public ResponseEntity<ApiResponse<List<PostImageResponse>>> getPostImages(
        @PathVariable Long postId) {

        List<PostImageResponse> responses = fileService.getPostImages(postId);
        return ResponseEntity.ok(ApiResponse.success(responses, "이미지 목록을 조회했습니다."));
    }

    // 이미지 파일 다운로드 (or 브라우저에서 보기)
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource resource = fileStorageUtil.loadFileAsResource(filename);

        String contentType = "application/octet-stream";
        try {
            contentType = Files.probeContentType(Paths.get(resource.getFilename()));
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
        } catch (IOException e) {
            log.warn("Content-Type 감지 실패, 기본 값 사용: {}", filename);
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
    }


}
