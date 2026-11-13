package it.echoeswar.management.user.repository;

import it.echoeswar.management.user.entity.Role;
import it.echoeswar.management.user.enumerated.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByRoleName(RoleName roleName);
}
