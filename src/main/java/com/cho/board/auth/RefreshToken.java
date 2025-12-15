package com.cho.board.auth;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class RefreshToken {

    @Id
    private String token;
    private String email;
    private LocalDateTime expiryDate;

}
