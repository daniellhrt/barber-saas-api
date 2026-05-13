package br.com.daniel.danbarbersaasapi.domain.barber;

import java.math.BigDecimal;
import java.util.UUID;

public record BarberResponseDTO(UUID id, String name, String phone, String specialty, BigDecimal commissionRate,
                                String email) {
    public BarberResponseDTO(Barber barber) {
        this(barber.getId(), barber.getName(), barber.getPhone(), barber.getSpecialty(), barber.getCommissionRate(), barber.getUser().getEmail());
    }
}