package com.dev.E_commerce.Mini.repository;

import com.dev.E_commerce.Mini.entity.Address;
import com.dev.E_commerce.Mini.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findAllByUser(User user);
    Optional<Address> findByIdAndUser(Long id, User user);
    List<Address> findAllByUserAndIsDefaultTrue(User user);
}
