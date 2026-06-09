package br.com.daniel.danbarbersaasapi.services;

import br.com.daniel.danbarbersaasapi.domain.barber.Barber;
import br.com.daniel.danbarbersaasapi.domain.barber.BarberRequestDTO;
import br.com.daniel.danbarbersaasapi.domain.user.User;
import br.com.daniel.danbarbersaasapi.domain.user.UserRole;
import br.com.daniel.danbarbersaasapi.infra.exception.ConflictException;
import br.com.daniel.danbarbersaasapi.infra.exception.ResourceNotFoundException;
import br.com.daniel.danbarbersaasapi.repository.BarberRepository;
import br.com.daniel.danbarbersaasapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BarberService {

    private final BarberRepository barberRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Cria um Usuário de acesso + perfil de Barbeiro em uma única transação.
     * Se qualquer passo falhar, tudo é revertido.
     */
    @Transactional
    public Barber create(BarberRequestDTO data) {
        if (userRepository.findByEmail(data.email()) != null) {
            throw new ConflictException("E-mail já cadastrado no sistema.");
        }

        String encryptedPassword = passwordEncoder.encode(data.password());
        User newUser = new User(data.email(), encryptedPassword, UserRole.BARBER);
        userRepository.save(newUser);

        Barber barber = new Barber();
        barber.setUser(newUser);
        barber.setName(data.name());
        barber.setPhone(data.phone());
        barber.setSpecialty(data.specialty());
        barber.setCommissionRate(data.commissionRate());

        return barberRepository.save(barber);
    }

    public List<Barber> findAll() {
        return barberRepository.findAll();
    }

    public Barber findById(UUID id) {
        return barberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barbeiro não encontrado."));
    }
}
