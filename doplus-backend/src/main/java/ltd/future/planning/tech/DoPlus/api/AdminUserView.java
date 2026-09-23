package ltd.future.planning.tech.DoPlus.api;

import lombok.Builder;

@Builder
public record AdminUserView(int id, String username, String firstName, String lastName, boolean isActive) {
}
