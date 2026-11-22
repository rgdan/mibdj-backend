package teccr.mibdj.data;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class Match {
    private Long matchId;
    private Long userId;
    private Long gameId;
    private Long placeId;
    private LocalDate fecha;
    private Integer duracionMin;
    private String notas;
    private List<MatchParticipant> participantes;
}

