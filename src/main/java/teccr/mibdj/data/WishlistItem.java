package teccr.mibdj.data;

import lombok.Data;

import java.time.LocalDate;

/**
 * Juego dentro de la lista de deseos de un usuario (tabla Desea).
 */
@Data
public class WishlistItem {
    private Long gameId;
    private String title;
    private String publisher;
    private Integer priority;
    private LocalDate date;
}
