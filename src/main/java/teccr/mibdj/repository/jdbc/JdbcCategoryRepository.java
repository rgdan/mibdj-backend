package teccr.mibdj.repository.jdbc;

import teccr.mibdj.repository.CategoryRepository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import teccr.mibdj.data.Category;

import java.util.List;

@Repository
public class JdbcCategoryRepository implements CategoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcCategoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Category> findAll() {
        return jdbcTemplate.query(
                "SELECT id_categoria, nombre FROM Categoria ORDER BY nombre",
                (resultSet, rowNumber) -> {
                    Category category = new Category();
                    category.setId(resultSet.getLong("id_categoria"));
                    category.setName(resultSet.getString("nombre"));
                    return category;
                });
    }
}
