package com.example.task2.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.task2.dto.AlbumInfoDto;
import com.example.task2.dto.ArtistInfoDto;
import com.example.task2.dto.SaveAlbumDto;
import com.example.task2.exception.custom.DuplicateNameException;
import com.example.task2.exception.custom.ResourceNotFoundException;
import com.example.task2.model.Album;
import com.example.task2.repository.AlbumRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistService artistService;

    public Album findById(UUID albumId) {
        return albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException("Album", albumId));
    }

    public AlbumInfoDto getAlbumDetails(UUID id) {
        Album album = findById(id);
        return convertToDetails(album);
    }

    @Transactional
    public UUID save(SaveAlbumDto saveAlbumDto) {
        if(albumRepository.existsByNameAndArtistId(saveAlbumDto.getName(), saveAlbumDto.getArtistId())) {
            throw new DuplicateNameException("Album", saveAlbumDto.getName());
        }
        Album album = Album.builder()
        .name(saveAlbumDto.getName())
        .artist(artistService.findById(saveAlbumDto.getArtistId())) 
        .build();

        return albumRepository.save(album).getId(); 
    }

    @Transactional
    public UUID update(UUID id, SaveAlbumDto saveAlbumDto) {
        if(albumRepository.existsByNameAndArtistId(saveAlbumDto.getName(), saveAlbumDto.getArtistId())) {
            throw new DuplicateNameException("Album", saveAlbumDto.getName());
        }
        Album albumToUpdate = findById(id);
        albumToUpdate.setName(saveAlbumDto.getName());
        albumToUpdate.setArtist(artistService.findById(saveAlbumDto.getArtistId()));
        return albumRepository.save(albumToUpdate).getId();
    }

    public void delete(UUID id) {
        Album album = findById(id);
        albumRepository.delete(album);
    }

    public AlbumInfoDto convertToDetails(Album album) {
        return new AlbumInfoDto(
                album.getId(),
                album.getName(),
                new ArtistInfoDto(album.getArtist().getId(), album.getArtist().getName())
        );
    }
}
