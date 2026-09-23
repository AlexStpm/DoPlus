package ltd.future.planning.tech.DoPlus.entity;


import lombok.RequiredArgsConstructor;

import java.util.*;

import java.util.List;

@RequiredArgsConstructor
public enum Role {
    ROOTADMIN,
    USER;

    public Collection<String> getAuthorities() {
        return switch (this) {
            case ROOTADMIN -> List.of("ROOTADMIN");
            case USER -> List.of("USER");
            default -> Collections.emptyList();
        };
    }

    public static Role getRoleByOrdinal(int ordinal) {
        return Role.values()[ordinal - 1];
    }
}
