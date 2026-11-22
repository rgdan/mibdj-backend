package teccr.mibdj.repository;



import teccr.mibdj.data.Match;
import teccr.mibdj.data.MatchParticipant;

import java.time.LocalDate;
import java.util.List;

public interface MatchRepository {
    boolean placeExists(long placeId);
    boolean placeBelongsToUser(long placeId, long userId);
    Match createMatch(long organizerUserId, long gameId, long placeId, LocalDate fecha,
                      int duracionMin, String notas, List<MatchParticipant> participantes);
}