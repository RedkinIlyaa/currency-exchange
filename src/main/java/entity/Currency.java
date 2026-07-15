package entity;

import lombok.*;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Currency {
    @EqualsAndHashCode.Exclude
    private Integer id;
    private String code;
    private String fullName;
    private String sign;
}
