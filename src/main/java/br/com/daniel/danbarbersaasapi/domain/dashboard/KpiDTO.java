package br.com.daniel.danbarbersaasapi.domain.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiDTO {
    private BigDecimal faturamentoDia;
    private BigDecimal faturamentoMes;
    private Long atendimentosHoje;
    private BigDecimal ticketMedio;
}
