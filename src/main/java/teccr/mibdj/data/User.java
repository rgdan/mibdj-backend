package teccr.mibdj.data;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String userName;
    private String name;
    private String email;
    private Type type;
    private LocalDateTime createdAt;

    public enum Type {
        ADMIN,
        REGULAR
    }
}
