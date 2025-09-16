package com.example.rent_it.ServiceImpl;

import com.example.rent_it.Dto.OwnerDto;
import com.example.rent_it.Entitys.Owner;
import com.example.rent_it.Service.OwnerService;
import com.example.rent_it.reposactory.OwnerRepo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OwnerServiceImpl implements OwnerService {

    @Autowired
private OwnerRepo ownerRepo;
    @Autowired
    private ModelMapper modelMapper;
    @Override
    public OwnerDto createOwner(OwnerDto ownerDto) {
        Owner owner=modelMapper.map(ownerDto,Owner.class);
       Owner saveOwner= ownerRepo.save(owner);
        return modelMapper.map(saveOwner,OwnerDto.class);
    }

    @Override
    public void deleteOwner(int ownerId) {
        ownerRepo.deleteById(ownerId);

    }
}
