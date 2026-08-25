package com.dev.E_commerce.Mini.mapper;

import com.dev.E_commerce.Mini.dto.request.AddressRequest;
import com.dev.E_commerce.Mini.dto.response.AddressResponse;
import com.dev.E_commerce.Mini.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    Address toAddress(AddressRequest request);
    AddressResponse toAddressResponse(Address address);
    void updateAddress(@MappingTarget Address address, AddressRequest request);
}
