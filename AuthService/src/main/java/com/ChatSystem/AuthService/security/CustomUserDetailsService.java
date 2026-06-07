package com.ChatSystem.AuthService.security;

import com.ChatSystem.AuthService.entity.AuthUser;
import com.ChatSystem.AuthService.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AuthUserRepository authUserRepository;

    /**
     * Spring Security calls this with whatever was passed to authenticate().
     * We support both username and email as the identifier.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        AuthUser user = authUserRepository.findByUsername(identifier)
                .or(() -> authUserRepository.findByEmail(identifier))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No account found for identifier: " + identifier));

        return new CustomUserDetails(user);
    }
}
