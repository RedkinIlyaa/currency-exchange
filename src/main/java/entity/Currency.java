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

    @EqualsAndHashCode.Exclude
    private String fullName;

    @EqualsAndHashCode.Exclude
    private String sign;
}
