package de.fhdortmund.mystudyapp.identity.service;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.fhdortmund.mystudyapp.identity.model.User;
import de.fhdortmund.mystudyapp.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String universityEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUniversityEmail(universityEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + universityEmail));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUniversityEmail())
                .password(user.getPasswordHash())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .accountLocked(user.getTrustLevel() == de.fhdortmund.mystudyapp.identity.model.TrustLevel.FLAGGED)
                .disabled(!user.isVerified())   // ← NEW: blocks login until verified
                .build();
    }
}