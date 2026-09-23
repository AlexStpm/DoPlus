package ltd.future.planning.tech.DoPlus.entity;

import lombok.*;
import org.springframework.data.annotation.Transient;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TimeManagement {
    @Transient
    @Getter
    @Setter
    private int timeManagementId;
    @Getter
    @Setter
    private int userId;
    @Getter
    @Setter
    private String dateTime;
    @Getter
    @Setter
    private int taskId;
    @Getter
    @Setter
    private String description;
}
