package com.example.task2.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SaveAlbumDto {

    @NotBlank
    @Size(min = 1, max = 50)
    private final String name;

    @NotNull
    private final UUID artistId;
}
