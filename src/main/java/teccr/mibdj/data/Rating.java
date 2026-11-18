package teccr.mibdj.data;

import lombok.Data;

/**
 * Calificación y comentario de un usuario sobre un juego (tabla Valora).
 */
@Data
public class Rating {
    private Long userId;
    private String userName;
    private Long gameId;
    private String gameTitle;
    private Integer rating;
    private String comment;
}
