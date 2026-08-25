package com.dev.E_commerce.Mini.service;

import com.dev.E_commerce.Mini.dto.request.AddressRequest;
import com.dev.E_commerce.Mini.dto.response.AddressResponse;
import com.dev.E_commerce.Mini.entity.Address;
import com.dev.E_commerce.Mini.entity.User;
import com.dev.E_commerce.Mini.exception.AppException;
import com.dev.E_commerce.Mini.exception.ErrorCode;
import com.dev.E_commerce.Mini.mapper.AddressMapper;
import com.dev.E_commerce.Mini.repository.AddressRepository;
import com.dev.E_commerce.Mini.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AddressService {
    AddressRepository addressRepository;
    UserRepository userRepository;
    AddressMapper addressMapper;

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getMyAddresses() {
        return addressRepository.findAllByUser(currentUser()).stream()
                .map(addressMapper::toAddressResponse)
                .toList();
    }

    @Transactional
    public AddressResponse createAddress(AddressRequest request) {
        User user = currentUser();
        Address address = addressMapper.toAddress(request);
        address.setUser(user);

        if (address.isDefault()) {
            unsetPreviousDefault(user);
        }
        return addressMapper.toAddressResponse(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse updateAddress(Long addressId, AddressRequest request) {
        User user = currentUser();
        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXISTED));

        addressMapper.updateAddress(address, request);

        if (address.isDefault()) {
            unsetPreviousDefault(user);
            address.setDefault(true);
        }
        return addressMapper.toAddressResponse(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(Long addressId) {
        User user = currentUser();
        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXISTED));
        addressRepository.delete(address);
    }

    private void unsetPreviousDefault(User user) {
        addressRepository.findAllByUserAndIsDefaultTrue(user)
                .forEach(a -> {
                    a.setDefault(false);
                    addressRepository.save(a);
                });
    }
}
