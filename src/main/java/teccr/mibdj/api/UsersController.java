package teccr.mibdj.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import teccr.mibdj.data.User;
import teccr.mibdj.service.UserService;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class UsersController {

    private final UserService userService;

    public UsersController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public Iterable<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> userOpt = userService.getUserById(id);
        return userOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/session")
    public ResponseEntity<User> login(@Valid @RequestBody SessionRequest request) {
        return userService.getUserByEmail(request.email())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record SessionRequest(@NotBlank @Email String email) {
        public SessionRequest {
            email = email == null ? null : email.trim();
        }
    }
}
