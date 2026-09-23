package ltd.future.planning.tech.DoPlus.service;


import lombok.RequiredArgsConstructor;
import ltd.future.planning.tech.DoPlus.dao.DAO;
import ltd.future.planning.tech.DoPlus.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class DoplusUserDetailsService implements UserDetailsService {
    @Autowired
    private final DAO DAO;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = DAO.getUserByUsername(username);
        return new org.springframework.security.core.userdetails.User(user.get().getUsername(), user.get().getPassword(), user.get().getAuthorities());

    }
}