package com.example.task2.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.task2.model.Album;

@Repository
public interface AlbumRepository extends JpaRepository<Album, UUID> {

    boolean existsByNameAndArtistId(String name, UUID artistId);

}
