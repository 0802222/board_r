package com.cho.board.file.service;

import com.cho.board.global.util.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileService {

    private final FileStorageUtil fileStorageUtil;

    // 프로필 이미지 업로드
    @Transactional
    public String uploadProfileImage(MultipartFile file) {
        return fileStorageUtil.storeProfileImage(file);
    }

    // 게시글 이미지 업로드
    public String uploadPostImage(MultipartFile file) {
        return fileStorageUtil.storePostImage(file);
    }

    // 파일 삭제
    public void deleteFile(String filename, boolean isProfile) {
        fileStorageUtil.deleteFile(filename, isProfile);
    }
}
