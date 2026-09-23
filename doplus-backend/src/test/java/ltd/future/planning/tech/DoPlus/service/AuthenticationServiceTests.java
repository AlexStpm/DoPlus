package ltd.future.planning.tech.DoPlus.service;

import ltd.future.planning.tech.DoPlus.api.CreateUserRequest;
import ltd.future.planning.tech.DoPlus.dao.DAO;
import ltd.future.planning.tech.DoPlus.dao.TokenDAO;
import ltd.future.planning.tech.DoPlus.api.AuthenticateUserRequest;
import ltd.future.planning.tech.DoPlus.entity.Token;
import ltd.future.planning.tech.DoPlus.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTests {
    @Mock
    private DAO mockRepository;

    @Mock
    private TokenDAO mockTokenRepository;

    @Mock
    private PasswordEncoder mockPasswordEncoder;

    @Mock
    private JwtService mockJwtService;

    @Mock
    private AuthenticationManager mockAuthenticationManager;

    @InjectMocks
    private AuthenticationService authService;

    //1 - /register
    @Test
    void userDoesNotExistsRegisterReturnsAuthenticationResponse() {
        //given user is not registered yet
        CreateUserRequest createUserRequest = new CreateUserRequest("username", "password", "firstName", "lastName");
        //when
        when(mockPasswordEncoder.encode(anyString())).thenReturn("encryptedPassword");
        when(mockRepository.getUserByUsername(anyString())).thenReturn(Optional.empty())
                .thenAnswer(invocation -> Optional.of(new User(1,
                        createUserRequest.getUsername(),
                        createUserRequest.getPassword(),
                        createUserRequest.getFirstName(),
                        createUserRequest.getLastName(), 1, 1)));
        doNothing().when(mockRepository).addUser(any(User.class));
        when(mockJwtService.generateToken(any(User.class))).thenReturn("jwtToken");
        var result = authService.register(createUserRequest);
        //then
        assertTrue(result.isPresent());
        assertEquals("jwtToken", result.get().getToken());
        assertEquals(createUserRequest.getUsername(), result.get().getUser().username());
    }

    @Test
    void userExistsRegisterReturnsOptionalEmpty() {
        //given user is already registered
        CreateUserRequest createUserRequest = new CreateUserRequest("username", "password", "firstName", "lastName");
        //when
        when(mockPasswordEncoder.encode(anyString())).thenReturn("encryptedPassword");
        when(mockRepository.getUserByUsername(anyString())).thenReturn(Optional.of(new User(1,
                createUserRequest.getUsername(),
                createUserRequest.getPassword(),
                createUserRequest.getFirstName(),
                createUserRequest.getLastName(), 1, 1)));
        var result = authService.register(createUserRequest);
        //then
        assertTrue(result.isEmpty());
    }

    //2- /authenticate
    @Test
    void validUserGivenAuthenticateRevokesAllUserTokensAndReturnsToken() {
        String username = "user";
        String password = "password";
        AuthenticateUserRequest request = new AuthenticateUserRequest(username, password);
        User user = new User(1, username, password, "First", "Last", 1, 1);
        String expectedToken = "jwtToken";
        List<Token> existingTokens = List.of(new Token(1, "oldToken1", 1), new Token(2, "oldToken2", 1));

        when(mockRepository.getUserByUsername(username)).thenReturn(Optional.of(user));
        when(mockJwtService.generateToken(user)).thenReturn(expectedToken);
        when(mockAuthenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(mockTokenRepository.findByUser(user.getUserId())).thenReturn(existingTokens);

        ResponseEntity<?> response = authService.authenticate(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        existingTokens.forEach(token ->
                verify(mockTokenRepository, times(1)).delete(token.getToken())
        );
    }

    @Test
    void invalidUserGivenAuthenticateReturnsIncorrectUsernameResponse() {
        String username = "invalidUser";
        String password = "wrongPassword";
        AuthenticateUserRequest request = new AuthenticateUserRequest(username, password);

        when(mockRepository.getUserByUsername(username)).thenReturn(Optional.empty());

        ResponseEntity<?> response = authService.authenticate(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Incorrect username", response.getBody());
    }

    @Test
    void inactiveUserGivenAuthenticateReturnsUserNotActiveResponse() {
        String username = "inactiveUser";
        String password = "password";
        AuthenticateUserRequest request = new AuthenticateUserRequest(username, password);
        User user = new User(1, username, password, "First", "Last", 1, 0);

        when(mockRepository.getUserByUsername(username)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = authService.authenticate(request);

        assertEquals(HttpStatus.GONE, response.getStatusCode());
        assertEquals("User is not active", response.getBody());
    }

}
