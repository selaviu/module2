package com.example.task2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SaveGenreDto {

    @NotBlank
    @Size(min = 1, max = 50)
    private final String name;
}
