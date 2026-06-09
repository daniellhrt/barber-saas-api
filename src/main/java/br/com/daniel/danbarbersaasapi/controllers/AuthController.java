package br.com.daniel.danbarbersaasapi.controllers;

import br.com.daniel.danbarbersaasapi.domain.user.*;
import br.com.daniel.danbarbersaasapi.infra.exception.ConflictException;
import br.com.daniel.danbarbersaasapi.repository.BarberRepository;
import br.com.daniel.danbarbersaasapi.repository.UserRepository;
import br.com.daniel.danbarbersaasapi.security.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final BarberRepository barberRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid AuthenticationDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);
        var token = tokenService.generateToken((User) auth.getPrincipal());
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    /**
     * Registro público: cria sempre um ADMIN (dono de barbearia).
     * Para criar barbeiros funcionários, use POST /barbers (requer ADMIN autenticado).
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterDTO data) {
        if (this.userRepository.findByEmail(data.email()) != null) {
            throw new ConflictException("E-mail já cadastrado no sistema.");
        }

        String encryptedPassword = passwordEncoder.encode(data.password());
        // Registro público sempre cria ADMIN (dono da barbearia)
        User newUser = new User(data.email(), encryptedPassword, UserRole.ADMIN);
        this.userRepository.save(newUser);

        return ResponseEntity.ok().build();
    }

    /**
     * Retorna os dados do usuário autenticado + perfil de barbeiro se existir.
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getProfile(@AuthenticationPrincipal User currentUser) {
        var barberProfile = barberRepository.findByUserId(currentUser.getId()).orElse(null);
        return ResponseEntity.ok(new UserProfileDTO(currentUser, barberProfile));
    }
}