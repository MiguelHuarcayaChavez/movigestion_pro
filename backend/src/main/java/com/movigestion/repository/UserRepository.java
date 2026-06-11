package com.movigestion.repository;

import com.movigestion.entity.Role;
import com.movigestion.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    boolean existsByDni(String dni);
    boolean existsByUsername(String username);
    boolean existsByCelular(String celular);
    List<User> findByRolAndEstado(Role rol, String estado);
}
