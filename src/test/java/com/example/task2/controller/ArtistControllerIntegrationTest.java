package com.example.task2.controller;

import com.example.task2.config.AbstractIntegrationTest;
import com.example.task2.dto.SaveArtistDto;
import com.example.task2.model.Artist;
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

public class ArtistControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ArtistRepository artistRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    private UUID createdArtistId;
    private final String API_PATH = "/api/artist"; 

    private String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() {
        artistRepository.deleteAll(); 
    }

    @BeforeEach
    void setupTestData() {
        artistRepository.deleteAll(); 

        Artist artist1 = Artist.builder().name("Test Artist 1").build();
        Artist artist2 = Artist.builder().name("Test Artist 2").build();
        artistRepository.saveAll(List.of(artist1, artist2));

        createdArtistId = artist1.getId();
    }

    @Test
    @DisplayName("POST /api/artist - Successful artist creation")
    void createArtist_Success() throws Exception {
        SaveArtistDto newArtistDto = new SaveArtistDto("New Test Artist Name"); 
        
        ResultActions result = mockMvc.perform(post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(newArtistDto)));

        result.andExpect(status().isCreated())
              .andExpect(jsonPath("$.result").exists());
    }

    @Test
    @DisplayName("POST /api/artist - Validation Failure (blank name)") 
    void createArtist_ValidationFailed_NameBlank() throws Exception {
        SaveArtistDto invalidArtist = new SaveArtistDto(" "); 

        mockMvc.perform(post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(invalidArtist)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name: must not be blank")); 
    }


    @Test
    @DisplayName("POST /api/artist - Failure: Duplicate Name (409 Conflict)")
    void createArtist_ValidationFailed_DuplicateName() throws Exception {
        SaveArtistDto invalidArtist = new SaveArtistDto("Test Artist 2"); 

        mockMvc.perform(post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(invalidArtist)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Artist with name Test Artist 2 already exists.")); 
    }

    @Test
    @DisplayName("GET /api/artist - Successful retrieval of all artists")
    void getAllArtists_Success() throws Exception {

        mockMvc.perform(get(API_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()) 
                .andExpect(jsonPath("$.length()").value(2)) 
                .andExpect(jsonPath("$[0].name").exists()) 
                .andExpect(jsonPath("$[0].name").value("Test Artist 1")); 
    }

    @Test
    @DisplayName("PUT /api/artist/{id} - Successful update of artist name")
    void updateArtist_Success() throws Exception {
        String UPDATED_NAME = "UPDATED_NAME";
        SaveArtistDto updateDto = new SaveArtistDto(UPDATED_NAME); 
        
        ResultActions result = mockMvc.perform(put(API_PATH + "/{id}", createdArtistId) 
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateDto)));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value(createdArtistId.toString())); 

        Artist updatedArtist = artistRepository.findById(createdArtistId).orElseThrow();
        
        assertEquals(UPDATED_NAME, updatedArtist.getName(), "Artist name should be updated in the database.");
    }

    @Test
    @DisplayName("PUT /api/artist/{id} - Validation Failure: Name too long (400)")
    void updateArtist_ValidationFailed_TooLongName() throws Exception {
        String UPDATED_NAME = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890"
                + "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890"
                + "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
        SaveArtistDto updateDto = new SaveArtistDto(UPDATED_NAME); 
        
        ResultActions result = mockMvc.perform(put(API_PATH + "/{id}", createdArtistId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateDto)));

        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("name: size must be between 0 and 100")); 

    }


    @Test
    @DisplayName("DELETE /api/artist/{id} - Successful artist deletion (204)")
    void deleteArtist_Success() throws Exception {
        
        ResultActions result = mockMvc.perform(delete(API_PATH + "/{id}", createdArtistId)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNoContent()); 

        Assertions.assertTrue(artistRepository.findById(createdArtistId).isEmpty(), 
            "Artist should be deleted from the database and Optional should be empty.");
    }

    @Test
    @DisplayName("DELETE /api/artist/{id} - Failure: Artist does not exist (404)")
    void deleteArtist_Failed_ArtistByIdNotExist() throws Exception {
        
        final UUID nonExistentId = UUID.randomUUID(); 

        mockMvc.perform(delete(API_PATH + "/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isNotFound())
                
                .andExpect(jsonPath("$.message").value("Artist with id '" + nonExistentId.toString() + "' not found."));
    }
}