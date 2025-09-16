package com.example.rent_it.Service;

import com.example.rent_it.Dto.OwnerDto;

public interface OwnerService {
    public OwnerDto createOwner(OwnerDto ownerDto);
    public void deleteOwner(int ownerId);
}
