package ltd.future.planning.tech.DoPlus.api;

import lombok.Builder;

@Builder
public record AuthView(int id,
                       String username,
                       String firstname,
                       String lastname) {
}

