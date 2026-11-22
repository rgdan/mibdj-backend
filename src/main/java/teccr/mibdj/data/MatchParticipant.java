package teccr.mibdj.data;


import lombok.Data;

@Data
public class MatchParticipant {

    private Long userId;
    private Integer puntos;
    private Boolean ganador;
}
