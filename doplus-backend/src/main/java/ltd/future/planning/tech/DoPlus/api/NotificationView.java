package ltd.future.planning.tech.DoPlus.api;

import lombok.Builder;

@Builder
public record NotificationView(int id, int taskId, boolean seen) {
}