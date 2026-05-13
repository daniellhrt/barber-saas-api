package br.com.daniel.danbarbersaasapi.controllers;

import br.com.daniel.danbarbersaasapi.domain.user.AuthenticationDTO;
import br.com.daniel.danbarbersaasapi.domain.user.LoginResponseDTO;
import br.com.daniel.danbarbersaasapi.domain.user.RegisterDTO;
import br.com.daniel.danbarbersaasapi.domain.user.User;
import br.com.daniel.danbarbersaasapi.repository.UserRepository;
import br.com.daniel.danbarbersaasapi.security.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository repository;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthenticationDTO data) {
        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
            // É nesta linha abaixo que o Spring vai no banco checar o hash!
            var auth = this.authenticationManager.authenticate(usernamePassword);

            var token = tokenService.generateToken((User) Objects.requireNonNull(auth.getPrincipal()));

            return ResponseEntity.ok(new LoginResponseDTO(token));

        } catch (Exception e) {
            // Se cair aqui, sabemos com 100% de certeza que o Java chegou na requisição,
            // mas a senha não bateu ou o e-mail não existe no Neon!
            return ResponseEntity.status(401).body("Erro de Autenticação: E-mail ou senha incorretos.");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterDTO data) {
        if (this.repository.findByEmail(data.email()) != null) {
            return ResponseEntity.badRequest().build();
        }

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        User newUser = new User(data.email(), encryptedPassword, data.role());

        this.repository.save(newUser);

        return ResponseEntity.ok().build();
    }
}