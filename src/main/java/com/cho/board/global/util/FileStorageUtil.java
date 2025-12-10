package com.cho.board.global.util;

import static com.cho.board.global.constants.FileConstants.ALLOWED_IMAGE_EXTENSIONS;
import static com.cho.board.global.constants.FileConstants.MAX_FILE_SIZE;

import com.cho.board.global.exception.ErrorCode;
import com.cho.board.global.exception.FileStorageException;
import com.cho.board.global.exception.ResourceNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileStorageUtil {

    private final Path profileStorageLocation;
    private final Path postStorageLocation;

    public FileStorageUtil(
        @Value("${file.profile-dir}") String profileDir,
        @Value("${file.post-dir}") String postDir) {

        this.profileStorageLocation = Paths.get(profileDir)
            .toAbsolutePath().normalize();
        this.postStorageLocation = Paths.get(postDir)
            .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.profileStorageLocation);
            Files.createDirectories(this.postStorageLocation);
        } catch (IOException ex) {
            throw new FileStorageException(
                "파일 저장 디렉토리를 생성할 수 없습니다.", ex);
        }
    }

    // 프로필 이미지 저장
    public String storeProfileImage(MultipartFile file) {
        return storeFile(file, profileStorageLocation);
    }

    // 게시글 이미지 저장
    public String storePostImage(MultipartFile file) {
        return storeFile(file, postStorageLocation);
    }

    // 파일 저장 (내부 메서드)
    private String storeFile(MultipartFile file, Path storageLocation) {
        // 1. 기본 검증
        validateFile(file);

        // 2. 파일명 정리 및 UUID 추가
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        String storedFilename = UUID.randomUUID().toString() + "." + fileExtension;

        try {
            // 경로 traversal 공격 방지
            if (originalFilename.contains("..")) {
                throw new FileStorageException(
                    ErrorCode.FILE_INVALID_PATH + ": " + originalFilename);
            }

            // 4. 파일 저장
            Path targetLocation = storageLocation.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return storedFilename;

        } catch (IOException ex) {
            throw new FileStorageException("파일 저장에 실패했습니다.: " + originalFilename, ex);
        } finally {

        }
    }

    // 파일 검증
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException(ErrorCode.FILE_EMPTY);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileStorageException(ErrorCode.FILE_TOO_LARGE);
        }

        String filename = file.getOriginalFilename();
        String extension = getFileExtension(filename);

        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new FileStorageException(ErrorCode.FILE_UNSUPPORTED_FORMAT);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new FileStorageException(ErrorCode.FILE_NO_EXTENSION);
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    public void deleteFile(String filename, boolean isProfile) {
        try {
            Path storageLocation = isProfile ? profileStorageLocation : postStorageLocation;
            Path filePath = storageLocation.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("파일 삭제에 실패했습니다: " + filename, ex);
        }
    }

    // 파일 리소스 로드 (다운로드/조회용)
    public Resource loadFileAsResource(String filename) {
        try {
            Path profilePath = profileStorageLocation.resolve(filename).normalize();
            Path postPath = postStorageLocation.resolve(filename).normalize();

            Resource resource;
            if (Files.exists(profilePath)) {
                resource = new UrlResource(profilePath.toUri());
            } else if (Files.exists(postPath)) {
                resource = new UrlResource(postPath.toUri());
            } else {
                throw new ResourceNotFoundException(
                    ErrorCode.RESOURCE_NOT_FOUND, "파일을 찾을 수 없습니다. : " + filename);
            }

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND,
                    "파일을 읽을 수 없습니다. " + filename);
            }
        } catch (MalformedURLException e) {
            throw new FileStorageException("파일 경로 오류: " + filename);
        }
    }

    // 파일 삭제
    public void deleteFile(String filename) {
        try {
            Path profilePath = profileStorageLocation.resolve(filename).normalize();
            Path postPath = postStorageLocation.resolve(filename).normalize();

            if (Files.exists(profilePath)) {
                Files.delete(profilePath);
            } else if (Files.exists(postPath)) {
                Files.delete(postPath);
            }
        } catch (IOException e) {
            throw new FileStorageException("파일 삭제 실패: " + filename);
        }
    }
}
