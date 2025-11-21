package com.cho.board.service.user;

import com.cho.board.controller.user.dtos.UserCreateRequest;
import com.cho.board.controller.user.dtos.UserUpdateRequest;
import com.cho.board.domain.user.Role;
import com.cho.board.domain.user.User;
import com.cho.board.global.exception.BusinessException;
import com.cho.board.global.exception.DuplicateResourceException;
import com.cho.board.global.exception.ErrorCode;
import com.cho.board.global.exception.ResourceNotFoundException;
import com.cho.board.repository.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User create(UserCreateRequest request) {

        // 존재하는 email & nickname 인지 확인
        if (userRepository.findByName(request.getNickname()).isPresent()) {
            throw new DuplicateResourceException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
            .name(request.getName())
            .nickname(request.getNickname())
            .email(request.getEmail())
            .password(encodedPassword)
            .profileImage(request.getProfileImage())
            .role(Role.USER)
            .build();
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User findById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(
                () -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다. ID: " + userId));
    }

    public User update(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다. ID: " + userId));

        user.updateProfile(request.getNickname(), request.getProfileImage());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new BusinessException(ErrorCode.SAME_PASSWORD);
            }

            String encodedPassword = passwordEncoder.encode(request.getPassword());
            user.changePassword(encodedPassword);
        }

        return user;
    }

    public void delete(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(IllegalArgumentException::new);

        userRepository.delete(user);
    }
}
