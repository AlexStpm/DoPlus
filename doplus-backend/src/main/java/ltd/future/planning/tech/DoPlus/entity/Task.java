package ltd.future.planning.tech.DoPlus.entity;

import lombok.*;
import org.springframework.data.annotation.Transient;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Task {
    @Transient
    @Getter
    @Setter
    private int taskId;
    @Getter
    @Setter
    private int assigneeId;
    @Getter
    @Setter
    private int reporterId;
    @Getter
    @Setter
    private int boardId;
    @Getter
    @Setter
    private String priority;
    @Getter
    @Setter
    private String severity;
    @Getter
    @Setter
    private int estimation;
    @Getter
    @Setter
    private String status;
    @Getter
    @Setter
    private String description;
    @Getter
    @Setter
    private String title;
    @Getter
    @Setter
    private String creationTime;
}
