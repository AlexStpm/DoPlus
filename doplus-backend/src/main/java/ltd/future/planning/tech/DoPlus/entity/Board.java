package ltd.future.planning.tech.DoPlus.entity;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Board {
    @Getter
    @Setter
    private int boardId;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private int isArchived;
}
