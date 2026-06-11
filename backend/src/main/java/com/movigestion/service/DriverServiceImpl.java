package com.movigestion.service;

import com.movigestion.dto.request.CreateDriverRequestDTO;
import com.movigestion.dto.response.DriverResponseDTO;
import com.movigestion.entity.Role;
import com.movigestion.entity.RoleEnum;
import com.movigestion.entity.User;
import com.movigestion.entity.UserStatusEnum;
import com.movigestion.exception.ResourceConflictException;
import com.movigestion.exception.ResourceNotFoundException;
import com.movigestion.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public DriverResponseDTO createDriver(CreateDriverRequestDTO dto) {
        if (userRepository.existsByDni(dto.getDni())) {
            throw new ResourceConflictException("El DNI ingresado ya se encuentra registrado.");
        }
        if (userRepository.existsByUsername(dto.getUsuario())) {
            throw new ResourceConflictException("El usuario ingresado ya se encuentra registrado.");
        }
        if (userRepository.existsByCelular(dto.getCelular())) {
            throw new ResourceConflictException("El celular ingresado ya se encuentra registrado.");
        }

        // Obtener ID del admin actual desde SecurityContextHolder
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User adminUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Administrador no encontrado en la sesión actual"));

        User newDriver = User.builder()
                .dni(dto.getDni())
                .username(dto.getUsuario())
                .password(passwordEncoder.encode(dto.getContrasena()))
                .celular(dto.getCelular())
                .profilePicture(dto.getFotografiaPerfil())
                .rol(Role.DRIVER) // Forzado por código
                .estado("ACTIVO") // Forzado por código
                .creatorAdmin(adminUser)
                .build();

        User savedDriver = userRepository.save(newDriver);
        return mapToResponseDTO(savedDriver);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverResponseDTO> findAllActiveDrivers() {
        List<User> drivers = userRepository.findByRolAndEstado(Role.DRIVER, "ACTIVO");
        return drivers.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DriverResponseDTO findDriverById(Integer id) {
        User driver = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado con ID: " + id));

        if (driver.getRol() != Role.DRIVER) {
            throw new ResourceNotFoundException("El usuario encontrado no es un conductor válido.");
        }

        return mapToResponseDTO(driver);
    }

    private DriverResponseDTO mapToResponseDTO(User user) {
        Integer creatorId = (user.getCreatorAdmin() != null) ? user.getCreatorAdmin().getId() : null;
        
        return DriverResponseDTO.builder()
                .idUsuario(user.getId())
                .dni(user.getDni())
                .usuario(user.getUsername())
                .celular(user.getCelular())
                .fotografiaPerfil(user.getProfilePicture())
                .rol(RoleEnum.valueOf(user.getRol().name()))
                .estado(UserStatusEnum.valueOf(user.getEstado()))
                .idAdminCreador(creatorId)
                .fechaCreacion(user.getCreationDate())
                .build();
    }
}
