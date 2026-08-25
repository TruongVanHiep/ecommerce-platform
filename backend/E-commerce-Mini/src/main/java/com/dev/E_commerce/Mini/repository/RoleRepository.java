package com.dev.E_commerce.Mini.repository;

import com.dev.E_commerce.Mini.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
}
