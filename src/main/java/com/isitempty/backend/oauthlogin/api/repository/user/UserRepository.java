package com.isitempty.backend.oauthlogin.api.repository.user;

import com.isitempty.backend.oauthlogin.api.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUserId(String userId);
    User findByEmail(String email);

    Optional<User> findByUsername(String username);

}