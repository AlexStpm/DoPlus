package ltd.future.planning.tech.DoPlus.service;


import lombok.RequiredArgsConstructor;
import ltd.future.planning.tech.DoPlus.api.AuthView;
import ltd.future.planning.tech.DoPlus.api.AuthenticateUserRequest;
import ltd.future.planning.tech.DoPlus.api.AuthenticationResponse;
import ltd.future.planning.tech.DoPlus.api.CreateUserRequest;
import ltd.future.planning.tech.DoPlus.dao.DAO;
import ltd.future.planning.tech.DoPlus.dao.TokenDAO;
import ltd.future.planning.tech.DoPlus.entity.Token;
import ltd.future.planning.tech.DoPlus.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final DAO repository;
    private final TokenDAO tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public Optional<AuthenticationResponse> register(CreateUserRequest request) {
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .roleId(2)
                .isActive(1)
                .build();
        var userExists = repository.getUserByUsername(user.getUsername());
        if (userExists.isPresent()) {
            return Optional.empty();
        }
        repository.addUser(user);
        var savedUser = repository.getUserByUsername(request.getUsername()).get();
        var jwtToken = jwtService.generateToken(user);
        saveUserToken(savedUser, jwtToken);
        var authView = AuthView.builder()
                .id(savedUser.getUserId())
                .username(savedUser.getUsername())
                .firstname(savedUser.getFirstName())
                .lastname(savedUser.getLastName())
                .build();
        return Optional.of(AuthenticationResponse.builder()
                .token(jwtToken)
                .user(authView)
                .build());
    }

    public ResponseEntity<?> authenticate(AuthenticateUserRequest request) {

        var user = repository.getUserByUsername(request.getUsername());
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Incorrect username");
        }
        if (user.get().getIsActive() == 0)
            return ResponseEntity.status(HttpStatus.GONE).body("User is not active");
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var jwtToken = jwtService.generateToken(user.get());
        revokeAllUserTokens(user.get());
        saveUserToken(user.get(), jwtToken);
        var authView = AuthView.builder()
                .id(user.get().getUserId())
                .username(user.get().getUsername())
                .firstname(user.get().getFirstName())
                .lastname(user.get().getLastName())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(AuthenticationResponse.builder()
                .token(jwtToken)
                .user(authView)
                .build());
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .userId(user.getUserId())
                .token(jwtToken)
                .build();
        tokenRepository.add(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findByUser(user.getUserId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            tokenRepository.delete(token.getToken());
        });
    }


}
