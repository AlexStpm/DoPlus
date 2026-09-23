package ltd.future.planning.tech.DoPlus.entity;

import lombok.*;
import org.springframework.data.annotation.Transient;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification {
    @Getter
    @Setter
    @Transient
    private int notificationId;
    @Getter
    @Setter
    private int userId;
    @Getter
    @Setter
    private int taskId;
    @Getter
    @Setter
    private int isSeen;
}
