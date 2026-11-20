package teccr.mibdj.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import teccr.mibdj.data.WishlistItem;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcWishlistRepository implements WishlistRepository {

    private static final String SELECT_ITEMS = """
            SELECT d.id_juego, j.titulo, j.editorial, d.prioridad, d.fecha
            FROM Desea d
            JOIN Juego j ON j.id_juego = d.id_juego
            """;

    private static final RowMapper<WishlistItem> ITEM_ROW_MAPPER =
            JdbcWishlistRepository::mapItem;

    private final JdbcTemplate jdbcTemplate;

    public JdbcWishlistRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<WishlistItem> findByUser(Long userId, Integer priority) {
        // Prioridad 1 primero; dentro de cada prioridad, lo agregado más recientemente primero
        String sql = SELECT_ITEMS + """
                WHERE d.id_usuario = ?
                  AND (? IS NULL OR d.prioridad = ?)
                ORDER BY d.prioridad, d.fecha DESC, j.titulo
                """;
        return jdbcTemplate.query(sql, ITEM_ROW_MAPPER, userId, priority, priority);
    }

    @Override
    public Optional<WishlistItem> findByUserAndGame(Long userId, Long gameId) {
        String sql = SELECT_ITEMS + "WHERE d.id_usuario = ? AND d.id_juego = ?";
        return jdbcTemplate.query(sql, ITEM_ROW_MAPPER, userId, gameId)
                .stream()
                .findFirst();
    }

    @Override
    public void insert(Long userId, Long gameId, Integer priority, LocalDate date) {
        jdbcTemplate.update(
                "INSERT INTO Desea (id_usuario, id_juego, prioridad, fecha) VALUES (?, ?, ?, ?)",
                userId, gameId, priority, Date.valueOf(date));
    }

    @Override
    public int update(Long userId, Long gameId, Integer priority, LocalDate date) {
        // Si no se envía fecha, se conserva la que ya estaba guardada
        return jdbcTemplate.update("""
                        UPDATE Desea
                        SET prioridad = ?, fecha = COALESCE(?, fecha)
                        WHERE id_usuario = ? AND id_juego = ?
                        """,
                priority, date == null ? null : Date.valueOf(date), userId, gameId);
    }

    @Override
    public int delete(Long userId, Long gameId) {
        return jdbcTemplate.update(
                "DELETE FROM Desea WHERE id_usuario = ? AND id_juego = ?",
                userId, gameId);
    }

    private static WishlistItem mapItem(ResultSet resultSet, int rowNumber) throws SQLException {
        WishlistItem item = new WishlistItem();
        item.setGameId(resultSet.getLong("id_juego"));
        item.setTitle(resultSet.getString("titulo"));
        item.setPublisher(resultSet.getString("editorial"));
        item.setPriority(resultSet.getInt("prioridad"));
        item.setDate(resultSet.getDate("fecha").toLocalDate());
        return item;
    }
}
