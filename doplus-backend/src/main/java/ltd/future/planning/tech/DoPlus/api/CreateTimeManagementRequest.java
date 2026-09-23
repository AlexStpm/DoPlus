package ltd.future.planning.tech.DoPlus.api;

import lombok.Builder;

@Builder
public record CreateTimeManagementRequest(int userId, String dateTime, int taskId, String description) {
}
