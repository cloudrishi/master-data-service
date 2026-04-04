package com.rish.masterdata.repository;

import com.rish.masterdata.entity.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCredentialsRepository
    extends JpaRepository<UserCredentials, String> {

    Optional<UserCredentials> findByUserId(String userId);

}
