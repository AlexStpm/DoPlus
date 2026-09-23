package ltd.future.planning.tech.DoPlus.api;

import lombok.Builder;

@Builder
public record CreateTagRequest(int boardId, String name) {
}
