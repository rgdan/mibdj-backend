package teccr.mibdj.repository.jdbc;

import teccr.mibdj.repository.RatingRepository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import teccr.mibdj.data.Rating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcRatingRepository implements RatingRepository {

    private static final String SELECT_RATINGS = """
            SELECT v.id_usuario, u.nombre, v.id_juego, j.titulo,
                   v.calificacion, v.comentario
            FROM Valora v
            JOIN Usuario u ON u.id_usuario = v.id_usuario
            JOIN Juego j ON j.id_juego = v.id_juego
            """;

    private static final RowMapper<Rating> RATING_ROW_MAPPER = JdbcRatingRepository::mapRating;

    private final JdbcTemplate jdbcTemplate;

    public JdbcRatingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Rating> findByGame(Long gameId) {
        String sql = SELECT_RATINGS + """
                WHERE v.id_juego = ?
                ORDER BY v.calificacion DESC, u.nombre
                """;
        return jdbcTemplate.query(sql, RATING_ROW_MAPPER, gameId);
    }

    @Override
    public Optional<Rating> findByUserAndGame(Long userId, Long gameId) {
        String sql = SELECT_RATINGS + "WHERE v.id_usuario = ? AND v.id_juego = ?";
        return jdbcTemplate.query(sql, RATING_ROW_MAPPER, userId, gameId)
                .stream()
                .findFirst();
    }

    @Override
    public void insert(Long userId, Long gameId, Integer rating, String comment) {
        jdbcTemplate.update(
                "INSERT INTO Valora (id_usuario, id_juego, calificacion, comentario) VALUES (?, ?, ?, ?)",
                userId, gameId, rating, comment);
    }

    @Override
    public int update(Long userId, Long gameId, Integer rating, String comment) {
        return jdbcTemplate.update("""
                        UPDATE Valora
                        SET calificacion = ?, comentario = ?
                        WHERE id_usuario = ? AND id_juego = ?
                        """,
                rating, comment, userId, gameId);
    }

    private static Rating mapRating(ResultSet resultSet, int rowNumber) throws SQLException {
        Rating rating = new Rating();
        rating.setUserId(resultSet.getLong("id_usuario"));
        rating.setUserName(resultSet.getString("nombre"));
        rating.setGameId(resultSet.getLong("id_juego"));
        rating.setGameTitle(resultSet.getString("titulo"));
        rating.setRating(resultSet.getInt("calificacion"));
        rating.setComment(resultSet.getString("comentario"));
        return rating;
    }
}
