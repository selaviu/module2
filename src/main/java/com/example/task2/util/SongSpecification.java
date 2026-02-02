package com.example.task2.util;

import org.springframework.data.jpa.domain.Specification;

import com.example.task2.model.Song;

public class SongSpecification {

    public static Specification<Song> isArist(String artistName) {
        return (root, query, builder) -> {
            if (artistName == null || artistName.trim().isEmpty()) {
                return null;
            }

            String pattern = "%" + artistName.toLowerCase() + "%";

            return builder.like(builder.lower(root.get("artist").get("name")), pattern);
        };
    }

    public static Specification<Song> isAlbum(String albumName) {
        return (root, query, builder) -> {
            if (albumName == null || albumName.trim().isEmpty()) {
                return null;
            }
            String pattern = "%" + albumName.toLowerCase() + "%";
            
            return builder.like(builder.lower(root.get("album").get("name")), pattern);
        };
    }

    public static Specification<Song> releasedInYear(Integer year){
        return(root, query, builder) -> {
            if(year == null){
                return null;
            }

            return builder.equal(root.get("releaseYear"), year);
        };
    }

    public static Specification<Song> isGenre(String genreName){
        return(root, query, builder) -> {
            if(genreName == null || genreName.trim().isEmpty()){
                return null;
            }

            String pattern = "%" + genreName.toLowerCase() + "%";

            return builder.like(builder.lower(root.get("genres").get("name")), pattern);
        };
    }
}
