package ltd.future.planning.tech.DoPlus.entity;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Token {
    @Getter
    @Setter
    private int tokenId;
    @Getter
    @Setter
    private String token;
    @Getter
    @Setter
    private int userId;
}
