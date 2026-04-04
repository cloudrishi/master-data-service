package com.rish.masterdata.repository;

import com.rish.masterdata.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository
    extends JpaRepository<Address, String> {

    List<Address> findByUserId(String userId);
}

