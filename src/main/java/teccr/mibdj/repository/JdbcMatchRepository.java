package teccr.mibdj.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import teccr.mibdj.data.Match;
import teccr.mibdj.data.MatchParticipant;

import java.sql.Date;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;

@Repository
public class JdbcMatchRepository implements MatchRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcMatchRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean placeExists(long placeId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Lugar WHERE id_lugar = ?",
                Integer.class,
                placeId
        );
        return count != null && count > 0;
    }

    @Override
    public boolean placeBelongsToUser(long placeId, long userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Lugar WHERE id_lugar = ? AND id_usuario = ?",
                Integer.class,
                placeId, userId
        );
        return count != null && count > 0;
    }

    @Override
    public Match createMatch(long organizerUserId, long gameId, long placeId, LocalDate fecha,
                             int duracionMin, String notas, List<MatchParticipant> participantes) {
        Long nextMatchId = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(id_partida), 0) + 1 FROM Partida",
                Long.class
        );
        long matchId = (nextMatchId != null) ? nextMatchId : 1L;

        String insertPartidaSql = """
                INSERT INTO Partida (id_partida, fecha, duracion_min, notas, id_usuario, id_juego, id_lugar)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                insertPartidaSql,
                new Object[]{
                        matchId,
                        Date.valueOf(fecha),
                        duracionMin,
                        (notas != null && !notas.isBlank()) ? notas : null,
                        organizerUserId,
                        gameId,
                        placeId
                },
                new int[]{
                        Types.INTEGER,
                        Types.DATE,
                        Types.INTEGER,
                        Types.VARCHAR,
                        Types.INTEGER,
                        Types.INTEGER,
                        Types.INTEGER
                }
        );

        String insertParticipaSql = """
                INSERT INTO Participa (id_partida, id_usuario, puntos, ganador)
                VALUES (?, ?, ?, ?)
                """;

        for (MatchParticipant p : participantes) {
            jdbcTemplate.update(
                    insertParticipaSql,
                    matchId,
                    p.getUserId(),
                    p.getPuntos(),
                    p.getGanador()
            );
        }

        Match match = new Match();
        match.setMatchId(matchId);
        match.setUserId(organizerUserId);
        match.setGameId(gameId);
        match.setPlaceId(placeId);
        match.setFecha(fecha);
        match.setDuracionMin(duracionMin);
        match.setNotas(notas);
        match.setParticipantes(participantes);
        return match;
    }
}