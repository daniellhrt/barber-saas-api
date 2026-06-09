package br.com.daniel.danbarbersaasapi.domain.client;

import br.com.daniel.danbarbersaasapi.domain.barber.Barber;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(length = 20)
    private String whatsapp;

    @Column(unique = true)
    private String email;

    @Column(unique = true, length = 14)
    private String cpf;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String address;

    /**
     * Intervalo de retorno esperado em dias.
     * Definido pelo barbeiro. Exemplo: 30 dias para clientes que cortam todo mês.
     * Usado no endpoint /clients/overdue para alertar sobre clientes sem retorno.
     */
    @Column(name = "return_interval_days")
    private Integer returnIntervalDays;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id")
    private Barber primaryBarber;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public void updateInfo(ClientUpdateDTO data) {
        if (data.name() != null) this.name = data.name();
        if (data.phone() != null) this.phone = data.phone();
        if (data.whatsapp() != null) this.whatsapp = data.whatsapp();
        if (data.email() != null) this.email = data.email();
        if (data.cpf() != null) this.cpf = data.cpf();
        if (data.birthDate() != null) this.birthDate = data.birthDate();
        if (data.notes() != null) this.notes = data.notes();
        if (data.address() != null) this.address = data.address();
        if (data.returnIntervalDays() != null) this.returnIntervalDays = data.returnIntervalDays();
    }
}