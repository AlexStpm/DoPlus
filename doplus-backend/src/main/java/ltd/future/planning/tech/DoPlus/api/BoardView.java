package ltd.future.planning.tech.DoPlus.api;

import lombok.Builder;

@Builder
public record BoardView(int id, String name, boolean isArchived) {
}