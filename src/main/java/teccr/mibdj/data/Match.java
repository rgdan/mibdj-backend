package teccr.mibdj.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Match {
    private Long matchId;
    private Long userId;
    private Long gameId;
    private String gameTitle;
    private Long placeId;
    private String placeName;
    private LocalDate fecha;
    private Integer duracionMin;
    private String notas;
    private Integer participantesCount;
    private List<MatchParticipant> participantes;
}