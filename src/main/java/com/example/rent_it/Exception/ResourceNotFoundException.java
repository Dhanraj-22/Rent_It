package com.example.rent_it.Exception;

import lombok.Getter;
import lombok.Setter;


@Getter
public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(String message){
        super(message);

    }

}
