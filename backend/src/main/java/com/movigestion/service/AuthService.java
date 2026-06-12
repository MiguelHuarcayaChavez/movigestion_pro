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
import com.movigestion.dto.response.AuthResponseDTO;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthResponseDTO login(LoginRequestDTO request) {
        // 1. Validar credenciales
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        // 2. Obtener tu entidad User real para sacar el ID y el Rol original
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 3. Crear el UserDetails para Spring Security (como ya lo hac as)
        final UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRol().name())
                .build();

        // 4. Generar el JWT
        String token = jwtUtil.generateToken(userDetails.getUsername(), userDetails.getAuthorities().iterator().next().getAuthority());

        // 5. Retornar el DTO con la estructura exacta que espera React
        return new AuthResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getRol().name(),
                token
        );
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
