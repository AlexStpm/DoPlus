package ltd.future.planning.tech.DoPlus.controller;


import lombok.RequiredArgsConstructor;
import ltd.future.planning.tech.DoPlus.api.AuthenticateUserRequest;
import ltd.future.planning.tech.DoPlus.api.AuthenticationResponse;
import ltd.future.planning.tech.DoPlus.api.CreateUserRequest;
import ltd.future.planning.tech.DoPlus.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody CreateUserRequest request
    ) {
        var response = service.register(request);
        return response.map(ResponseEntity::ok).orElseGet(()
                -> ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(
            @RequestBody AuthenticateUserRequest request
    ) {
        if (request.getUsername().isEmpty() || request.getPassword().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing Username or Password");
        }
        return service.authenticate(request);
    }


}