package com.example.task2.dto;

import java.util.List;
import java.util.UUID;

import com.example.task2.validation.constraints.NotFutureYear;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SaveSongDto {

    @NotBlank
    @Size(min = 1, max = 100)
    private String title;

    @NotNull
    private UUID artistId;

    @Min(1900)
    @NotFutureYear
    private int releaseYear;

    @Min(1)
    private int duration;

    @NotNull
    private UUID albumId;

    @NotEmpty
    private List<UUID> genresId;
}
