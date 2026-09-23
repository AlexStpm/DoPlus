package ltd.future.planning.tech.DoPlus.api;

import java.util.List;

public record UpdateTaskRequest(String title,
                                int assigneeId,
                                int reporterId,
                                String priority,
                                String severity,
                                int estimation,
                                String status,
                                String description,
                                List<Integer> tags) {
}
