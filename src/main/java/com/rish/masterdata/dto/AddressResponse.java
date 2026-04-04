package com.rish.masterdata.dto;

import com.rish.masterdata.entity.AddressType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    private String id;
    private AddressType addressType;
    private String street;
    private String street2;
    private String city;
    private String state;
    private String zip;
    private String country;
    private Boolean isDefault;
}