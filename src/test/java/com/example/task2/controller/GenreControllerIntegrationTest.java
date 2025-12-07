package com.example.task2.controller;

import com.example.task2.config.AbstractIntegrationTest;
import com.example.task2.dto.SaveGenreDto; 
import com.example.task2.model.Genre;
import com.example.task2.repository.GenreRepository;
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

public class GenreControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private GenreRepository genreRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    private UUID createdGenreId;
    private final String API_PATH = "/api/genre";

    private String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() {
        genreRepository.deleteAll(); 
    }

    @BeforeEach
    void setupTestData() {
        genreRepository.deleteAll(); 

        Genre genre1 = Genre.builder().name("Test Genre 1").build();
        Genre genre2 = Genre.builder().name("Test Genre 2").build();
        
        genreRepository.saveAll(List.of(genre1, genre2));

        createdGenreId = genre1.getId();
    }
    
    @Test
    @DisplayName("POST /api/genre - Successful genre creation (201)")
    void createGenre_Success() throws Exception { 
        SaveGenreDto newGenreDto = new SaveGenreDto("New Test Genre Name"); 
        
        ResultActions result = mockMvc.perform(post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(newGenreDto)));

        result.andExpect(status().isCreated())
              .andExpect(jsonPath("$.result").exists());
    }

    @Test
    @DisplayName("POST /api/genre - Validation Failure (blank name)")
    void createGenre_ValidationFailed_NameBlank() throws Exception {

        SaveGenreDto invalidGenre = new SaveGenreDto(" "); 

        mockMvc.perform(post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(invalidGenre)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name: must not be blank")); 
    }


    @Test
    @DisplayName("POST /api/genre - Failure: Duplicate Name (409 Conflict)")
    void createGenre_ValidationFailed_DuplicateName() throws Exception {
        SaveGenreDto invalidGenre = new SaveGenreDto("Test Genre 1"); 

        mockMvc.perform(post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(invalidGenre)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Genre with name Test Genre 1 already exists.")); 
    }

    @Test
    @DisplayName("GET /api/genre - Successful retrieval of genre list") 
    void getAllGenres_Success() throws Exception { 

        mockMvc.perform(get(API_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()) 
                .andExpect(jsonPath("$.length()").value(2));
    }

    
    @Test
    @DisplayName("PUT /api/genre/{id} - Successful update of genre name")
    void updateGenre_Success() throws Exception {
        String UPDATED_NAME = "UPDATED_NAME";
        SaveGenreDto updateDto = new SaveGenreDto(UPDATED_NAME); 
        
        ResultActions result = mockMvc.perform(put(API_PATH + "/{id}", createdGenreId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateDto)));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value(createdGenreId.toString())); 

        Genre updatedGenre = genreRepository.findById(createdGenreId).orElseThrow();
        
        assertEquals(UPDATED_NAME, updatedGenre.getName(), "Genre name should be updated in the database.");
    }

    @Test
    @DisplayName("PUT /api/genre/{id} - Validation Failure: Name too long (400)")
    void updateGenre_ValidationFailed_TooLongName() throws Exception {
        String tooLongName = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890"
                + "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890"
                + "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
       
        SaveGenreDto updateDto = new SaveGenreDto(tooLongName); 
        
        mockMvc.perform(put(API_PATH + "/{id}", createdGenreId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name: size must be between 1 and 50")); 
    }


    @Test
    @DisplayName("DELETE /api/genre/{id} - Successful genre deletion (204)")
    void deleteGenre_Success() throws Exception { 
        
        mockMvc.perform(delete(API_PATH + "/{id}", createdGenreId)
                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isNoContent()); 

        Assertions.assertTrue(genreRepository.findById(createdGenreId).isEmpty(), 
            "Genre should be deleted from the database and Optional should be empty.");
    }

    @Test
    @DisplayName("DELETE /api/genre/{id} - Failure: Genre does not exist (404)")
    void deleteGenre_Failed_GenreByIdNotExist() throws Exception { 
        
        final UUID nonExistentId = UUID.randomUUID(); 

        mockMvc.perform(delete(API_PATH + "/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isNotFound())
            
                .andExpect(jsonPath("$.message").value("Genre with id '" + nonExistentId.toString() + "' not found."));
    }
}