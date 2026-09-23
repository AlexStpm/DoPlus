package ltd.future.planning.tech.DoPlus.entity;


import java.util.Collection;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.stream.Collectors;


@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements UserDetails {

    @Getter
    @Setter
    private int userId;
    @Getter
    @Setter
    private String username;
    @Getter
    @Setter
    private String password;
    @Getter
    @Setter
    private String firstName;
    @Getter
    @Setter
    private String lastName;
    @Getter
    @Setter
    private int roleId;
    @Getter
    @Setter
    private int isActive;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Role.getRoleByOrdinal(roleId)
                .getAuthorities()
                .stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive == 1;
    }


}
