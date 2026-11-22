package teccr.mibdj.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import teccr.mibdj.data.User;

import teccr.mibdj.api.UsersController.AverageDurationByGame;
import teccr.mibdj.api.UsersController.FrequentCompanion;
import teccr.mibdj.api.UsersController.MostPlayedGame;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcUserRepository implements UserRepository {

    private static final String SELECT_COLUMNS = "id_usuario, nombre, email, pais, fecha";
    private static final RowMapper<User> USER_ROW_MAPPER = JdbcUserRepository::mapUser;

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query("SELECT " + SELECT_COLUMNS + " FROM Usuario", USER_ROW_MAPPER);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jdbcTemplate.query("SELECT " + SELECT_COLUMNS + " FROM Usuario WHERE id_usuario = ?",
                        USER_ROW_MAPPER, id)
                .stream()
                .findFirst();
    }

    public Optional<User> findByEmail(String email) {
        return jdbcTemplate.query("SELECT " + SELECT_COLUMNS + " FROM Usuario WHERE email = ?",
                        USER_ROW_MAPPER, email)
                .stream()
                .findFirst();
    }

    private static User mapUser(ResultSet resultSet, int rowNumber) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("id_usuario"));
        user.setName(resultSet.getString("nombre"));
        user.setEmail(resultSet.getString("email"));
        user.setCountry(resultSet.getString("pais"));
        user.setDate(resultSet.getDate("fecha").toLocalDate());
        return user;
    }


    @Override
    public List<MostPlayedGame> findMostPlayedGames(long userId) {
        String sql = """
                SELECT
                    j.id_juego,
                    j.titulo,
                    COUNT(DISTINCT p.id_partida) AS partidas_jugadas
                FROM Participa par
                JOIN Partida p ON par.id_partida = p.id_partida
                JOIN Juego j ON p.id_juego = j.id_juego
                WHERE par.id_usuario = ?
                GROUP BY j.id_juego, j.titulo
                ORDER BY partidas_jugadas DESC, j.titulo ASC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new MostPlayedGame(
                rs.getLong("id_juego"),
                rs.getString("titulo"),
                rs.getInt("partidas_jugadas")
        ), userId);
    }

    @Override
    public List<AverageDurationByGame> findAverageDurationByGame(long userId) {
        String sql = """
                SELECT
                    j.id_juego,
                    j.titulo,
                    ROUND(AVG(p.duracion_min), 2) AS duracion_promedio
                FROM Participa par
                JOIN Partida p ON par.id_partida = p.id_partida
                JOIN Juego j ON p.id_juego = j.id_juego
                WHERE par.id_usuario = ?
                GROUP BY j.id_juego, j.titulo
                ORDER BY duracion_promedio DESC, j.titulo ASC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new AverageDurationByGame(
                rs.getLong("id_juego"),
                rs.getString("titulo"),
                rs.getDouble("duracion_promedio")
        ), userId);
    }

    @Override
    public List<FrequentCompanion> findFrequentCompanions(long userId) {
        String sql = """
                SELECT
                    u.id_usuario,
                    u.nombre,
                    COUNT(DISTINCT p_usuario.id_partida) AS partidas_compartidas
                FROM Participa p_usuario
                JOIN Participa p_companero
                    ON p_usuario.id_partida = p_companero.id_partida
                JOIN Usuario u
                    ON p_companero.id_usuario = u.id_usuario
                WHERE p_usuario.id_usuario = ?
                  AND p_companero.id_usuario <> ?
                GROUP BY u.id_usuario, u.nombre
                ORDER BY partidas_compartidas DESC, u.nombre ASC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new FrequentCompanion(
                rs.getLong("id_usuario"),
                rs.getString("nombre"),
                rs.getInt("partidas_compartidas")
        ), userId, userId);
    }
}
