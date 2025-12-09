package com.urbansphere.authservice.repository;

import com.urbansphere.authservice.entity.LoginAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginAttemptRepository extends JpaRepository<LoginAttemptEntity, Integer> {
}
