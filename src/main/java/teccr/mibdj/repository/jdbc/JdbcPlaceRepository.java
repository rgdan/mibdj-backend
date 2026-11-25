package teccr.mibdj.repository.jdbc;

import teccr.mibdj.repository.PlaceRepository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import teccr.mibdj.data.Place;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@Repository
public class JdbcPlaceRepository implements PlaceRepository {

    private static final String SELECT_COLUMNS = "id_lugar, nombre, direccion";
    private static final RowMapper<Place> PLACE_ROW_MAPPER = JdbcPlaceRepository::mapPlace;

    private final JdbcTemplate jdbcTemplate;

    public JdbcPlaceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Place> findByUserId(Long userId) {
        return jdbcTemplate.query(
                "SELECT " + SELECT_COLUMNS +  " FROM Lugar WHERE id_usuario = ? ORDER BY nombre ASC",
                PLACE_ROW_MAPPER,
                userId
        );
    }

    @Override
    public Place insert(Long userId, String name, String address) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(
                    "INSERT INTO Lugar (id_usuario, nombre, direccion) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, userId);
            statement.setString(2, name);
            statement.setString(3, address);
            return statement;
        }, keys);

        Place place = new Place();
        place.setPlaceId(Objects.requireNonNull(keys.getKey()).longValue());
        place.setName(name);
        place.setAddress(address);
        return place;
    }

    private static Place mapPlace(ResultSet resultSet, int rowNumber) throws SQLException {
        Place place = new Place();
        place.setPlaceId(resultSet.getLong("id_lugar"));
        place.setName(resultSet.getString("nombre"));
        place.setAddress(resultSet.getString("direccion"));
        return place;
    }
}


