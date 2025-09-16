package com.example.rent_it.reposactory;

import com.example.rent_it.Entitys.Media;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaRepo extends JpaRepository<Media,Integer> {
}
