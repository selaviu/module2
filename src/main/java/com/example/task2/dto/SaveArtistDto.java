package com.example.task2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SaveArtistDto {

    @NotBlank
    @Size(min = 0, max = 100)
    private final String name;
}
