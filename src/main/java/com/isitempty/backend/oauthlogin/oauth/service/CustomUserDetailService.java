package com.isitempty.backend.oauthlogin.oauth.service;

import com.isitempty.backend.oauthlogin.api.entity.user.User;
import com.isitempty.backend.oauthlogin.api.repository.user.UserRepository;
import com.isitempty.backend.oauthlogin.oauth.entity.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserId(username);
        if (user == null) {
            throw new UsernameNotFoundException("Can not find username.");
        }
        return UserPrincipal.create(user);
    }
}

