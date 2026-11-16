package teccr.mibdj.data;

import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    private Long id;
    private String name;
    private String email;
    private String country;
    private LocalDate date;
}
