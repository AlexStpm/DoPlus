package ltd.future.planning.tech.DoPlus.entity;

import lombok.*;
import org.springframework.data.annotation.Transient;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Tag {
    @Getter
    @Setter
    @Transient
    private int tagId;
    @Getter
    @Setter
    private int boardId;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private int isActive;
}
