package com.example.task2.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.task2.model.Artist;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, UUID>{

    boolean existsByName(String name); 
    boolean existsByNameAndIdNot(String name, UUID id);
}
