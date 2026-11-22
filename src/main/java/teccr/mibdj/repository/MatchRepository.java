package teccr.mibdj.repository;

import teccr.mibdj.data.Match;
import teccr.mibdj.data.MatchParticipant;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MatchRepository {
    boolean placeExists(long placeId);

    boolean placeBelongsToUser(long placeId, long userId);

    Match createMatch(long UserId, long gameId, long placeId, LocalDate fecha,
                      int duracionMin, String notas, List<MatchParticipant> participantes);

    List<Match> findMatchesByUserId(long userId);
    Optional<Match> findMatchDetailForUser(long userId, long matchId);

}