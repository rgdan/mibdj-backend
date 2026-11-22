package teccr.mibdj.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import teccr.mibdj.data.Place;


import java.sql.ResultSet;
import java.sql.SQLException;
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

    private static Place mapPlace(ResultSet resultSet, int rowNumber) throws SQLException {
        Place place = new Place();
        place.setPlaceId(resultSet.getLong("id_lugar"));
        place.setName(resultSet.getString("nombre"));
        place.setAddress(resultSet.getString("direccion"));
        return place;
    }
}


