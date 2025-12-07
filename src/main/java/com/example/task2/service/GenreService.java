package com.example.task2.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.task2.dto.GenreInfoDto;
import com.example.task2.dto.SaveGenreDto;
import com.example.task2.exception.custom.DuplicateNameException;
import com.example.task2.exception.custom.ResourceNotFoundException;
import com.example.task2.model.Genre;
import com.example.task2.repository.GenreRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;

    public Genre findById(UUID id){
        return genreRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Genre", id));
    }

    public List<Genre> findAllById(List<UUID> ids){
        
        List<Genre> foundGenres = genreRepository.findAllById(ids);
        
        if (foundGenres.size() != ids.size()) {
            
            List<UUID> foundIds = foundGenres.stream()
                .map(Genre::getId)
                .collect(Collectors.toList());
            
            String missingIds = ids.stream()
                .filter(id -> !foundIds.contains(id))
                .map(UUID::toString)
                .collect(Collectors.joining(", "));
                
            throw new ResourceNotFoundException("Genre(s) with ID(s) not found:", missingIds);
        }
        
        return foundGenres;
    }

    public GenreInfoDto convertToDetails(Genre genre){
        return new GenreInfoDto(
            genre.getId(),
            genre.getName()
        );
    }

    public List<GenreInfoDto> findAll(){
        return genreRepository.findAll().stream()
        .map(g -> convertToDetails(g)).collect(Collectors.toList());
    }

    @Transactional
    public UUID save(SaveGenreDto saveGenreDto){
        if(genreRepository.existsByName(saveGenreDto.getName())){
            throw new DuplicateNameException("Genre", saveGenreDto.getName());
        }
        Genre genre = Genre.builder()
        .name(saveGenreDto.getName())
        .build();
        return genreRepository.save(genre).getId();
    }

    @Transactional
    public UUID update(UUID id, SaveGenreDto saveGenreDto){
        if(genreRepository.existsByNameAndIdNot(saveGenreDto.getName(), id)){
            throw new DuplicateNameException("Genre", saveGenreDto.getName());
        }
        Genre genreToUpdate = findById(id);
        genreToUpdate.setName(saveGenreDto.getName());
        return genreRepository.save(genreToUpdate).getId();
    }

    public void delete(UUID id){
        Genre genre = findById(id);
        genreRepository.delete(genre);
    }
}
