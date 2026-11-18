package teccr.mibdj.data;

import lombok.Data;

import java.time.LocalDate;

/**
 * Juego dentro de la colección personal de un usuario (tabla Posee).
 */
@Data
public class CollectionItem {
    private Long gameId;
    private String title;
    private String publisher;
    private LocalDate date;
    private String status;
}
