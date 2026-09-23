package ltd.future.planning.tech.DoPlus.api;

import lombok.Builder;

@Builder
public record UserView(int id, String username, String name) {
}
