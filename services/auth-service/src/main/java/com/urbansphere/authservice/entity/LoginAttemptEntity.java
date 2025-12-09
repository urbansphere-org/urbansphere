package com.urbansphere.authservice.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginAttemptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Email
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private boolean success;

    @Column(length = 50)
    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
