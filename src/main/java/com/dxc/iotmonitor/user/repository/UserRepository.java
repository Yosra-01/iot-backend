package com.dxc.iotmonitor.user.repository;

import com.dxc.iotmonitor.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmailIgnoreCase(String email);
    User findByEmailIgnoreCase(String email);

}
