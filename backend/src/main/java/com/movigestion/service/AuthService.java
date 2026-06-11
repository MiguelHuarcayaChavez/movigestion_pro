package com.movigestion.service;

import com.movigestion.dto.request.LoginRequestDTO;
import com.movigestion.dto.request.RegisterAdminRequestDTO;
import com.movigestion.dto.response.UserResponseDTO;
import com.movigestion.entity.Role;
import com.movigestion.entity.User;
import com.movigestion.exception.DataConflictException;
import com.movigestion.repository.UserRepository;
import com.movigestion.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public String login(LoginRequestDTO request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        final UserDetails user = userRepository.findByUsername(request.getUsername()).map(u ->
                org.springframework.security.core.userdetails.User.builder()
                        .username(u.getUsername())
                        .password(u.getPassword())
                        .roles(u.getRol().name())
                        .build()
        ).orElseThrow();
        return jwtUtil.generateToken(user.getUsername(), user.getAuthorities().iterator().next().getAuthority());
    }

    public UserResponseDTO registerAdmin(RegisterAdminRequestDTO request) {
        if (userRepository.existsByDni(request.getDni()) || userRepository.existsByUsername(request.getUsername()) || userRepository.existsByCelular(request.getCelular())) {
            throw new DataConflictException("Los datos proporcionados (DNI, celular o usuario) ya se encuentran registrados en el sistema");
        }

        User user = User.builder()
                .dni(request.getDni())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .celular(request.getCelular())
                .rol(Role.ADMIN)
                .build();

        user = userRepository.save(user);

        return UserResponseDTO.builder()
                .id(user.getId())
                .dni(user.getDni())
                .username(user.getUsername())
                .celular(user.getCelular())
                .rol(user.getRol().name())
                .creationDate(user.getCreationDate())
                .estado(user.getEstado())
                .build();
    }
}
