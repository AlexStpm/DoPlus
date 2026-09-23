package ltd.future.planning.tech.DoPlus.api;

import lombok.Builder;

@Builder
public record TimeManagementView(int id, String dateTime, int taskId, String description) {
}