package com.cho.board.controller.user;

import com.cho.board.controller.user.dtos.UserCreateRequest;
import com.cho.board.controller.user.dtos.UserDetailResponse;
import com.cho.board.controller.user.dtos.UserListResponse;
import com.cho.board.controller.user.dtos.UserUpdateRequest;
import com.cho.board.domain.user.User;
import com.cho.board.service.user.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDetailResponse> create(@RequestBody @Valid UserCreateRequest request) {
        User user = userService.create(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(UserDetailResponse.from(user));
    }

    @GetMapping
    public ResponseEntity<List<UserListResponse>> findAll() {
        List<UserListResponse> users = userService.findAll()
            .stream()
            .map(UserListResponse::from)
            .toList();

        return ResponseEntity.ok(users);
    }

    @GetMapping(("/{id}"))
    public ResponseEntity<UserDetailResponse> find(@PathVariable Long id){
        User user = userService.findById(id);

        return ResponseEntity.ok(UserDetailResponse.from(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDetailResponse> update(@PathVariable Long id,
                        @Valid @RequestBody UserUpdateRequest request) {
        User user = userService.update(id, request);
        
        return ResponseEntity.ok(UserDetailResponse.from(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
