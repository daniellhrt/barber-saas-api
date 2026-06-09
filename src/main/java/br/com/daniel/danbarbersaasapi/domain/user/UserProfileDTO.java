package br.com.daniel.danbarbersaasapi.domain.user;

import br.com.daniel.danbarbersaasapi.domain.barber.Barber;

import java.util.UUID;

/**
 * DTO de resposta para o endpoint GET /auth/me.
 * Retorna dados do usuário autenticado e seu perfil de barbeiro, se existir.
 */
public record UserProfileDTO(
        UUID userId,
        String email,
        String role,
        UUID barberId,
        String barberName,
        String barberPhone,
        String barberSpecialty
) {
    public UserProfileDTO(User user, Barber barber) {
        this(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                barber != null ? barber.getId() : null,
                barber != null ? barber.getName() : null,
                barber != null ? barber.getPhone() : null,
                barber != null ? barber.getSpecialty() : null
        );
    }
}
