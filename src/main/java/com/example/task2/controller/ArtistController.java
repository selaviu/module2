package com.example.task2.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.task2.dto.ArtistInfoDto;
import com.example.task2.dto.RestResponse;
import com.example.task2.dto.SaveArtistDto;
import com.example.task2.service.ArtistService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/artist")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;

    @GetMapping
    public List<ArtistInfoDto> getAllArtists(){
        return artistService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse createArtists(@Valid @RequestBody SaveArtistDto saveArtistDto){ 
        UUID id = artistService.save(saveArtistDto);
        return new RestResponse(id.toString());
    }

    @PutMapping("/{id}")
    public RestResponse updateArtist(@PathVariable UUID id, @Valid @RequestBody SaveArtistDto saveArtistDto){
        UUID artistId = artistService.update(id, saveArtistDto);
        return new RestResponse(artistId.toString());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteArtist(@PathVariable UUID id){
        artistService.delete(id);
    }

}
