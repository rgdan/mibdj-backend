package teccr.mibdj.data;

import lombok.Data;

import java.util.List;

/**
 * Todas las valoraciones de un juego, con su promedio y cantidad.
 */
@Data
public class GameRatings {
    private Long gameId;
    private String gameTitle;
    private Double average;
    private Integer count;
    private List<Rating> ratings = List.of();
}
