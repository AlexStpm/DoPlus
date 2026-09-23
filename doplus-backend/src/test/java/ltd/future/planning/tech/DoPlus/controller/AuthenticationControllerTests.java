package ltd.future.planning.tech.DoPlus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ltd.future.planning.tech.DoPlus.api.AuthView;
import ltd.future.planning.tech.DoPlus.api.AuthenticationResponse;
import ltd.future.planning.tech.DoPlus.api.CreateUserRequest;
import ltd.future.planning.tech.DoPlus.api.AuthenticateUserRequest;
import ltd.future.planning.tech.DoPlus.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationControllerTests {
    private MockMvc mockMvc;

    @Mock
    private AuthenticationService mockAuthService;

    @InjectMocks
    private AuthenticationController authController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    //1 - /register
    @Test
    void userDoesNotExistsRegisterReturnsOK() throws Exception {
        //given user is not registered yet
        CreateUserRequest createUserRequest = new CreateUserRequest("username", "password", "firstName", "lastName");
        //when
        when(mockAuthService.register(any(CreateUserRequest.class))).thenReturn(Optional.of(AuthenticationResponse.builder()
                .token("jwtToken")
                .user(AuthView.builder()
                        .id(1)
                        .username(createUserRequest.getUsername())
                        .firstname(createUserRequest.getFirstName())
                        .lastname(createUserRequest.getLastName())
                        .build())
                .build()));
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(createUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("jwtToken"));
        //then
        verify(mockAuthService, times(1)).register(any(CreateUserRequest.class));
    }

    @Test
    void userExistsRegisterReturnsConflict() throws Exception {
        //given user is already registered
        CreateUserRequest createUserRequest = new CreateUserRequest("username", "password", "firstName", "lastName");
        //when
        when(mockAuthService.register(any(CreateUserRequest.class))).thenReturn(Optional.empty());
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(createUserRequest)))
                .andExpect(status().isConflict());
        //then
        verify(mockAuthService, times(1)).register(any(CreateUserRequest.class));
    }

    //2- /authenticate
    @Test
    public void validCredentialsGivenAuthenticateReturnsToken() throws Exception {
        String requestBody = "{\"username\":\"user\",\"password\":\"pass\"}";

        AuthenticationResponse mockResponse = new AuthenticationResponse("dummyToken123", new AuthView(1, "user", "First", "Last"));

        doReturn(ResponseEntity.ok(mockResponse))
                .when(mockAuthService).authenticate(any(AuthenticateUserRequest.class));

        mockMvc.perform(post("/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("dummyToken123"));
    }

    @Test
    public void missingCredentialsGivenAuthenticateReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\", \"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Missing Username or Password"));
    }

    @Test
    public void incorrectUsernameGivenAuthenticateReturnsNotFound() throws Exception {
        doReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Incorrect username")).when(mockAuthService).authenticate(any(AuthenticateUserRequest.class));

        mockMvc.perform(post("/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"unknown\", \"password\":\"password\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Incorrect username"));
    }

    @Test
    public void inactiveUserGivenAuthenticateReturnsGone() throws Exception {
        doReturn(ResponseEntity.status(HttpStatus.GONE).body("User is not active")).when(mockAuthService).authenticate(any(AuthenticateUserRequest.class));

        mockMvc.perform(post("/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"inactiveUser\", \"password\":\"password\"}"))
                .andExpect(status().isGone())
                .andExpect(content().string("User is not active"));
    }

}


