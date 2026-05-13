package br.com.daniel.danbarbersaasapi.controllers;

import br.com.daniel.danbarbersaasapi.domain.barber.Barber;
import br.com.daniel.danbarbersaasapi.domain.barber.BarberRequestDTO;
import br.com.daniel.danbarbersaasapi.domain.barber.BarberResponseDTO;
import br.com.daniel.danbarbersaasapi.domain.user.User;
import br.com.daniel.danbarbersaasapi.domain.user.UserRole;
import br.com.daniel.danbarbersaasapi.repository.BarberRepository;
import br.com.daniel.danbarbersaasapi.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/barbers")
public class BarberController {

    private final BarberRepository barberRepository;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public BarberController(BarberRepository barberRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.barberRepository = barberRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    @Transactional // Garante que se der erro, ele desfaz a criação do User e do Barber
    public ResponseEntity<?> create(@RequestBody @Valid BarberRequestDTO data, UriComponentsBuilder uriBuilder) {
        // 1. Verifica se o e-mail já existe
        if (userRepository.findByEmail(data.email()) != null) {
            return ResponseEntity.badRequest().body("E-mail já cadastrado no sistema.");
        }

        // 2. Cria o Usuário de acesso para o Barbeiro
        String encryptedPassword = this.passwordEncoder.encode(data.password());
        User newUser = new User(data.email(), encryptedPassword, UserRole.BARBER);
        userRepository.save(newUser);

        // 3. Cria o perfil do Barbeiro vinculado ao Usuário
        Barber barber = new Barber();
        barber.setUser(newUser);
        barber.setName(data.name());
        barber.setPhone(data.phone());
        barber.setSpecialty(data.specialty());
        barber.setCommissionRate(data.commissionRate());
        barberRepository.save(barber);

        var uri = uriBuilder.path("/barbers/{id}").buildAndExpand(barber.getId()).toUri();
        return ResponseEntity.created(uri).body(new BarberResponseDTO(barber));
    }

    @GetMapping
    public ResponseEntity<List<BarberResponseDTO>> listAll() {
        var barbers = barberRepository.findAll().stream().map(BarberResponseDTO::new).toList();
        return ResponseEntity.ok(barbers);
    }
}