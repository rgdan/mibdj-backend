package teccr.mibdj.data;

import lombok.Data;

import java.util.List;

@Data
public class Game {
    private Long id;
    private String title;
    private String publisher;
    private Integer publicationYear;
    private Integer minPlayers;
    private Integer maxPlayers;
    private Integer durationMin;
    private List<Category> categories = List.of();
}
