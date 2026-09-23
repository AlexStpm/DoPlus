package ltd.future.planning.tech.DoPlus.api;

import lombok.Builder;

import java.util.List;

@Builder
public record TaskView(int id, String title, String assignee, String reporter, String priority, String severity
        , int estimation, String status, String description, List<Integer> tags, String creationTime, int boardId) {
}