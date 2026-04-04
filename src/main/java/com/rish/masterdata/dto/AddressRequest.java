package com.rish.masterdata.dto;

import com.rish.masterdata.entity.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    @NotNull(message = "Address type is required")
    private AddressType addressType;

    @NotBlank(message = "Street is required")
    private String street;

    private String street2;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Zip is required")
    private String zip;

    @NotBlank(message = "Country is required")
    private String country;

    private Boolean isDefault;
}