package com.example.task2.controller;

import com.example.task2.config.AbstractIntegrationTest;
import com.example.task2.dto.SaveAlbumDto;
import com.example.task2.model.Album;
import com.example.task2.model.Artist;
import com.example.task2.repository.AlbumRepository;
import com.example.task2.repository.ArtistRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

public class AlbumControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private ArtistRepository artistRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    private UUID createdAlbumId;
    private UUID createdArtistId;
    private final String API_PATH = "/api/album";

    private String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() {
        albumRepository.deleteAll(); 
        artistRepository.deleteAll();
    }

    @BeforeEach
    void setupTestData() {
        albumRepository.deleteAll(); 
        artistRepository.deleteAll();

        Artist artist1 = Artist.builder().name("Test Artist 1").build();
        artistRepository.save(artist1);

        Album album1 = Album.builder().name("Test Album 1").artist(artist1).build();
        Album album2 = Album.builder().name("Test Album 2").artist(artist1).build();
        
        albumRepository.saveAll(List.of(album1, album2));

        createdAlbumId = album1.getId();
        createdArtistId = artist1.getId();
    }


    @Test
    @DisplayName("POST /api/album - Successful album creation")
    void createAlbum_Success() throws Exception {
        SaveAlbumDto newAlbumDto = new SaveAlbumDto("New Test Album Name", createdArtistId); 
        
        ResultActions result = mockMvc.perform(post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(newAlbumDto)));

        result.andExpect(status().isCreated())
              .andExpect(jsonPath("$.result").exists());
    }

    @Test
    @DisplayName("POST /api/album - Failure: Duplicate Name (409 Conflict)")
    void createAlbum_Failed_DuplicateName() throws Exception {
        
        SaveAlbumDto duplicateAlbumDto = new SaveAlbumDto("Test Album 1", createdArtistId); 
        
        ResultActions result = mockMvc.perform(post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(duplicateAlbumDto)));

        result.andExpect(status().isConflict())            
            .andExpect(jsonPath("$.message").value("Album with name Test Album 1 already exists.")); 
    }

    @Test
    @DisplayName("POST /api/album - Validation Failure (blank name)")
    void createAlbum_ValidationFailed_NameBlank() throws Exception {
        SaveAlbumDto invalidAlbum = new SaveAlbumDto(" ", createdArtistId); 

        mockMvc.perform(post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(invalidAlbum)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name: must not be blank")); 
    }
    
    
    @Test
    @DisplayName("GET /api/album/{id} - Successful retrieval of album details")
    void getAlbum_Success() throws Exception {

        mockMvc.perform(get(API_PATH + "/{id}", createdAlbumId)
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Album 1"))
                .andExpect(jsonPath("$.artist.name").value("Test Artist 1")); 
    }
    

    @Test
    @DisplayName("PUT /api/album/{id} - Successful update of album name")
    void updateAlbum_Success() throws Exception {
        final String UPDATED_NAME = "UPDATED_ALBUM_NAME";
        SaveAlbumDto updateDto = new SaveAlbumDto(UPDATED_NAME, createdArtistId); 
        
        ResultActions result = mockMvc.perform(put(API_PATH + "/{id}", createdAlbumId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateDto)));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value(createdAlbumId.toString())); 

        Album updatedAlbum = albumRepository.findById(createdAlbumId).orElseThrow();
        
        assertEquals(UPDATED_NAME, updatedAlbum.getName(), "Album name should be updated in the database.");
    }

    @Test
    @DisplayName("PUT /api/album/{id} - Validation Failure: Name too long (400)")
    void updateAlbum_ValidationFailed_TooLongName() throws Exception {
        String tooLongName = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890" + "ABCDEABCDEаbcde";
        SaveAlbumDto updateDto = new SaveAlbumDto(tooLongName, createdArtistId); 
        
        mockMvc.perform(put(API_PATH + "/{id}", createdAlbumId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateDto)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name: size must be between 1 and 50")); 
    }


    @Test
    @DisplayName("DELETE /api/album/{id} - Successful album deletion (204)")
    void deleteAlbum_Success() throws Exception {
        
        mockMvc.perform(delete(API_PATH + "/{id}", createdAlbumId)
                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isNoContent());

        Assertions.assertTrue(albumRepository.findById(createdAlbumId).isEmpty(), 
            "Album should be deleted from the database.");
    }

    @Test
    @DisplayName("DELETE /api/album/{id} - Failure: Album does not exist (404)") 
    void deleteAlbum_Failed_AlbumByIdNotExist() throws Exception {
        
        final UUID nonExistentId = UUID.randomUUID(); 

        mockMvc.perform(delete(API_PATH + "/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isNotFound())
                
                .andExpect(jsonPath("$.message").value("Album with id '" + nonExistentId.toString() + "' not found."));
    }
}