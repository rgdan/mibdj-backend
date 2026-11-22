package teccr.mibdj.repository.jdbc;

import teccr.mibdj.repository.CollectionRepository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import teccr.mibdj.data.CollectionItem;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcCollectionRepository implements CollectionRepository {

    private static final String SELECT_ITEMS = """
            SELECT p.id_juego, j.titulo, j.editorial, p.fecha, p.estado
            FROM Posee p
            JOIN Juego j ON j.id_juego = p.id_juego
            """;

    private static final RowMapper<CollectionItem> ITEM_ROW_MAPPER =
            JdbcCollectionRepository::mapItem;

    private final JdbcTemplate jdbcTemplate;

    public JdbcCollectionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<CollectionItem> findByUser(Long userId, String status) {
        String sql = SELECT_ITEMS + """
                WHERE p.id_usuario = ?
                  AND (? IS NULL OR p.estado = ?)
                ORDER BY j.titulo
                """;
        return jdbcTemplate.query(sql, ITEM_ROW_MAPPER, userId, status, status);
    }

    @Override
    public Optional<CollectionItem> findByUserAndGame(Long userId, Long gameId) {
        String sql = SELECT_ITEMS + "WHERE p.id_usuario = ? AND p.id_juego = ?";
        return jdbcTemplate.query(sql, ITEM_ROW_MAPPER, userId, gameId)
                .stream()
                .findFirst();
    }

    @Override
    public void insert(Long userId, Long gameId, LocalDate date, String status) {
        jdbcTemplate.update(
                "INSERT INTO Posee (id_usuario, id_juego, fecha, estado) VALUES (?, ?, ?, ?)",
                userId, gameId, Date.valueOf(date), status);
    }

    @Override
    public int update(Long userId, Long gameId, LocalDate date, String status) {
        // Si no se envía fecha, se conserva la que ya estaba guardada
        return jdbcTemplate.update("""
                        UPDATE Posee
                        SET estado = ?, fecha = COALESCE(?, fecha)
                        WHERE id_usuario = ? AND id_juego = ?
                        """,
                status, date == null ? null : Date.valueOf(date), userId, gameId);
    }

    @Override
    public int delete(Long userId, Long gameId) {
        return jdbcTemplate.update(
                "DELETE FROM Posee WHERE id_usuario = ? AND id_juego = ?",
                userId, gameId);
    }

    private static CollectionItem mapItem(ResultSet resultSet, int rowNumber) throws SQLException {
        CollectionItem item = new CollectionItem();
        item.setGameId(resultSet.getLong("id_juego"));
        item.setTitle(resultSet.getString("titulo"));
        item.setPublisher(resultSet.getString("editorial"));
        item.setDate(resultSet.getDate("fecha").toLocalDate());
        item.setStatus(resultSet.getString("estado"));
        return item;
    }
}
