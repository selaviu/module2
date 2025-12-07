package com.example.task2.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.task2.dto.ArtistInfoDto;
import com.example.task2.dto.SaveArtistDto;
import com.example.task2.exception.custom.DuplicateNameException;
import com.example.task2.exception.custom.ResourceNotFoundException;
import com.example.task2.model.Artist;
import com.example.task2.repository.ArtistRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;

    public Artist findById(UUID id){
        return artistRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Artist", id));
    }

    @Transactional
    public UUID save(SaveArtistDto saveArtistDto){

        if (artistRepository.existsByName(saveArtistDto.getName())) {
            throw new DuplicateNameException("Artist", saveArtistDto.getName());
        }

        Artist artist = convertToEntity(saveArtistDto);
        return artistRepository.save(artist).getId();
    }

    public List<ArtistInfoDto> findAll(){
        return artistRepository.findAll().stream()
        .map(a -> convertToDetails(a)).collect(Collectors.toList());
    }

    @Transactional
    public UUID update(UUID id, SaveArtistDto saveArtistDto){
        if (artistRepository.existsByNameAndIdNot(saveArtistDto.getName(), id)) {
            throw new DuplicateNameException("Artist", saveArtistDto.getName());
        }
        Artist artistToUpdate = findById(id);
        updateFromDto(artistToUpdate, saveArtistDto);
        return artistRepository.save(artistToUpdate).getId();

    }

    public void delete(UUID id){
        Artist artist = findById(id);
        artistRepository.delete(artist);
    }

    private Artist convertToEntity(SaveArtistDto saveArtistDto){
        Artist artist = Artist.builder()
        .name(saveArtistDto.getName())
        .build();

        return artist;
    }

    private ArtistInfoDto convertToDetails(Artist artist){
        ArtistInfoDto artistInfoDto = ArtistInfoDto.builder()
        .name(artist.getName())
        .build();

        return artistInfoDto;
    }

    private Artist updateFromDto(Artist artistToUpdate, SaveArtistDto saveArtistDto){
        artistToUpdate.setName(saveArtistDto.getName());

        return artistToUpdate;
    }

    
}
