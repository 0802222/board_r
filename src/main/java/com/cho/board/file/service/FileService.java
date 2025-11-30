package com.cho.board.file.service;

import com.cho.board.global.exception.ErrorCode;
import com.cho.board.global.exception.FileStorageException;
import com.cho.board.global.exception.ResourceNotFoundException;
import com.cho.board.global.exception.UnauthorizedException;
import com.cho.board.global.util.FileStorageUtil;
import com.cho.board.post.dtos.PostImageResponse;
import com.cho.board.post.entity.Post;
import com.cho.board.post.entity.PostImage;
import com.cho.board.post.repository.PostImageRepository;
import com.cho.board.post.repository.PostRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileService {

    private final FileStorageUtil fileStorageUtil;
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;

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

    /* 게시글 이미지 */

    // 복수 업로드
    @Transactional
    public List<PostImageResponse> uploadPostImages(Long postId, List<MultipartFile> files) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND
                ,"게시글을 찾을 수 없습니다."));

        long currentImageCount = postImageRepository.countByPostId(postId);
        if (currentImageCount + files.size() > 10) {
            throw new FileStorageException("게시글당 최대 10개의 이미지만 업로드할 수 있습니다.");
        }

        List<PostImageResponse> responses = new ArrayList<>();
        int startOrder = (int) currentImageCount;

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String storedFilename = fileStorageUtil.storePostImage(file);

            PostImage postImage = PostImage.builder()
                .post(post)
                .filePath(storedFilename)
                .originalFilename(file.getOriginalFilename())
                .fileSize(file.getSize())
                .displayOrder(startOrder + i)
                .build();

            post.addImage(postImage);
            PostImage saved = postImageRepository.save(postImage);
            responses.add(PostImageResponse.from(saved));
        }

        return responses;
    }

    // 게시글 이미지 삭제
    @Transactional
    public void deletePostImage(Long postId, Long imageId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND,
                "게시글을 찾을 수 없습니다."));

        PostImage image = postImageRepository.findById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND,
                "이미 삭제되었거나 존재하지 않는 이미지 입니다."));

        if (!image.getPost().getId().equals(postId)) {
            throw new UnauthorizedException("해당 게시글의 이미지가 아닙니다.");
        }

        fileStorageUtil.deleteFile(image.getFilePath());

        post.removeImage(image);
        postImageRepository.delete(image);
    }

    // 게시글의 모든 이미지 조회
    public List<PostImageResponse> getPostImages(Long postId) {
        postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND,
                "게시물을 찾을 수 없습니다."));
        List<PostImage> images = postImageRepository.findByPostIdOrderByDisplayOrder(postId);

        return images.stream()
            .map(PostImageResponse::from)
            .collect(Collectors.toList());
    }

}
