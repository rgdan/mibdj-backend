package teccr.mibdj.repository.jdbc;

import teccr.mibdj.repository.GameRepository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import teccr.mibdj.data.Category;
import teccr.mibdj.data.Game;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcGameRepository implements GameRepository {

    private static final String SELECT_GAMES = """
            SELECT g.id_juego, g.titulo, g.editorial, g.anio_publicacion,
                   g.min_jugadores, g.max_jugadores, g.duracion_min,
                   c.id_categoria, c.nombre AS category_name
            FROM Juego g
            LEFT JOIN Juego_categoria gc ON gc.id_juego = g.id_juego
            LEFT JOIN Categoria c ON c.id_categoria = gc.id_categoria
            """;

    private static final ResultSetExtractor<List<Game>> GAME_EXTRACTOR =
            JdbcGameRepository::mapGames;

    private final JdbcTemplate jdbcTemplate;

    public JdbcGameRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Game> findAll(String title, Long categoryId) {
        String sql = SELECT_GAMES + """
                WHERE (? IS NULL OR g.titulo LIKE ?)
                  AND (? IS NULL OR EXISTS (
                      SELECT 1 FROM Juego_categoria filter_category
                      WHERE filter_category.id_juego = g.id_juego
                        AND filter_category.id_categoria = ?
                  ))
                ORDER BY g.id_juego, c.id_categoria
                """;
        String pattern = title == null ? null : "%" + title + "%";
        return jdbcTemplate.query(
                sql, GAME_EXTRACTOR, title, pattern, categoryId, categoryId);
    }

    @Override
    public Optional<Game> findById(Long id) {
        String sql = SELECT_GAMES + """
                WHERE g.id_juego = ?
                ORDER BY c.id_categoria
                """;
        return jdbcTemplate.query(sql, GAME_EXTRACTOR, id)
                .stream()
                .findFirst();
    }

    private static List<Game> mapGames(ResultSet resultSet) throws SQLException {
        Map<Long, Game> games = new LinkedHashMap<>();

        while (resultSet.next()) {
            long gameId = resultSet.getLong("id_juego");
            Game game = games.get(gameId);

            if (game == null) {
                game = new Game();
                game.setId(gameId);
                game.setTitle(resultSet.getString("titulo"));
                game.setPublisher(resultSet.getString("editorial"));
                game.setPublicationYear(resultSet.getInt("anio_publicacion"));
                game.setMinPlayers(resultSet.getInt("min_jugadores"));
                game.setMaxPlayers(resultSet.getInt("max_jugadores"));
                game.setDurationMin(resultSet.getInt("duracion_min"));
                game.setCategories(new ArrayList<>());
                games.put(gameId, game);
            }

            long categoryId = resultSet.getLong("id_categoria");
            if (!resultSet.wasNull()) {
                Category category = new Category();
                category.setId(categoryId);
                category.setName(resultSet.getString("category_name"));
                game.getCategories().add(category);
            }
        }

        return new ArrayList<>(games.values());
    }
}
