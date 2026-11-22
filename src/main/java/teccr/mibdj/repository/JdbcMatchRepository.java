package teccr.mibdj.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import teccr.mibdj.data.Match;
import teccr.mibdj.data.MatchParticipant;

import java.sql.Date;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    @Override
    public List<Match> findMatchesByUserId(long userId) {
        String sql = """
                SELECT DISTINCT
                    v.partida AS id_partida,
                    p.id_usuario AS id_organizador,
                    v.juego AS titulo_juego,
                    v.lugar AS nombre_lugar,
                    p.fecha,
                    p.duracion_min,
                    v.participantes AS total_participantes
                FROM vw_partidas_usuario v
                JOIN Partida p ON v.partida = p.id_partida
                LEFT JOIN Participa par ON p.id_partida = par.id_partida
                WHERE p.id_usuario = ? OR par.id_usuario = ?
                ORDER BY p.fecha DESC, v.partida DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Match match = new Match();
            match.setMatchId(rs.getLong("id_partida"));
            match.setUserId(rs.getLong("id_organizador"));
            match.setGameTitle(rs.getString("titulo_juego"));
            match.setPlaceName(rs.getString("nombre_lugar"));
            match.setFecha(rs.getDate("fecha").toLocalDate());
            match.setDuracionMin(rs.getInt("duracion_min"));
            match.setParticipantesCount(rs.getInt("total_participantes"));
            return match;
        }, userId, userId);
    }

    @Override
    public Optional<Match> findMatchDetailForUser(long userId, long matchId) {
        String matchSql = """
                SELECT
                    p.id_partida,
                    p.id_usuario AS id_organizador,
                    p.id_juego,
                    j.titulo AS titulo_juego,
                    p.id_lugar,
                    l.nombre AS nombre_lugar,
                    p.fecha,
                    p.duracion_min,
                    p.notas
                FROM Partida p
                JOIN Juego j ON p.id_juego = j.id_juego
                JOIN Lugar l ON p.id_lugar = l.id_lugar
                WHERE p.id_partida = ?
                  AND (
                      p.id_usuario = ?
                      OR EXISTS (
                          SELECT 1
                          FROM Participa par
                          WHERE par.id_partida = p.id_partida
                            AND par.id_usuario = ?
                      )
                  )
                """;

        List<Match> matches = jdbcTemplate.query(matchSql, (rs, rowNum) -> {
            Match match = new Match();
            match.setMatchId(rs.getLong("id_partida"));
            match.setUserId(rs.getLong("id_organizador"));
            match.setGameId(rs.getLong("id_juego"));
            match.setGameTitle(rs.getString("titulo_juego"));
            match.setPlaceId(rs.getLong("id_lugar"));
            match.setPlaceName(rs.getString("nombre_lugar"));
            match.setFecha(rs.getDate("fecha").toLocalDate());
            match.setDuracionMin(rs.getInt("duracion_min"));
            match.setNotas(rs.getString("notas"));
            return match;
        }, matchId, userId, userId);

        if (matches.isEmpty()) {
            return Optional.empty();
        }

        Match match = matches.get(0);

        String participantsSql = """
                SELECT id_usuario, puntos, ganador
                FROM Participa
                WHERE id_partida = ?
                ORDER BY ganador DESC, puntos DESC, id_usuario ASC
                """;

        List<MatchParticipant> participantes = jdbcTemplate.query(participantsSql, (rs, rowNum) -> {
            MatchParticipant mp = new MatchParticipant();
            mp.setUserId(rs.getLong("id_usuario"));
            mp.setPuntos(rs.getInt("puntos"));
            mp.setGanador(rs.getBoolean("ganador"));
            return mp;
        }, matchId);

        match.setParticipantes(participantes);
        return Optional.of(match);
    }
}