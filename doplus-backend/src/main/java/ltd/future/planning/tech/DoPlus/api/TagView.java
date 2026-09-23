package ltd.future.planning.tech.DoPlus.api;

import lombok.Builder;

@Builder
public record TagView(int id, String name, boolean isActive) {
}
