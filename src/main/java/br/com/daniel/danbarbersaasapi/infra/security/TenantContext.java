package br.com.daniel.danbarbersaasapi.infra.security;

import br.com.daniel.danbarbersaasapi.domain.barber.Barber;
import br.com.daniel.danbarbersaasapi.domain.user.User;
import br.com.daniel.danbarbersaasapi.infra.exception.BusinessException;
import br.com.daniel.danbarbersaasapi.repository.BarberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolve o Barber (tenant/dono) do usuário autenticado.
 *
 * Em um sistema multi-tenant onde cada barbeiro é independente,
 * este componente é injetado nos Services para filtrar dados
 * por owner_barber_id automaticamente.
 *
 * Uso:
 *   Barber owner = tenantContext.getCurrentBarber();
 *   // usar owner.getId() em queries filtradas
 */
@Component
@RequiredArgsConstructor
public class TenantContext {

    private final BarberRepository barberRepository;

    /**
     * Retorna o Barber vinculado ao usuário logado.
     * Lança exceção se o usuário não tiver perfil de barbeiro.
     */
    public Barber getCurrentBarber() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            throw new BusinessException("Usuário não autenticado.");
        }

        return barberRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException(
                        "Usuário não tem perfil de barbeiro cadastrado. Crie um barbeiro primeiro."));
    }

    /**
     * Retorna o User autenticado diretamente do SecurityContext.
     */
    public User getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            throw new BusinessException("Usuário não autenticado.");
        }
        return user;
    }
}
