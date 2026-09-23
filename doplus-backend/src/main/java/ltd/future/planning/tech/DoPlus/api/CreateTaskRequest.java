package ltd.future.planning.tech.DoPlus.api;

import lombok.Builder;

import java.util.List;

@Builder
public record CreateTaskRequest(String title,
                                int assigneeId,
                                int reporterId,
                                int boardId,
                                String priority,
                                String severity,
                                int estimation,
                                String status,
                                String description,
                                List<Integer> tagIds,
                                String creationTime) {
}
