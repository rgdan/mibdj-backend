package teccr.mibdj.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import teccr.mibdj.data.User;

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
}
