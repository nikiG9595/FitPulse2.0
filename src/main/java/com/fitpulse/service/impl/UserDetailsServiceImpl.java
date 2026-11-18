package com.fitpulse.service.impl;

import com.fitpulse.model.entity.User;
import com.fitpulse.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(identifier)
                .or(() -> findUniqueEmail(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("Потребителят не е намерен."));
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    private Optional<User> findUniqueEmail(String identifier) {
        if (!identifier.contains("@")) return Optional.empty();
        var matches = userRepository.findTop2ByEmailIgnoreCase(identifier);
        return matches.size() == 1 ? Optional.of(matches.getFirst()) : Optional.empty();
    }
}
