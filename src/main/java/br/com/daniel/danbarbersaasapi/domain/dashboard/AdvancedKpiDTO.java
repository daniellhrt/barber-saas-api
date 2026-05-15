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
public class AdvancedKpiDTO {
    private BigDecimal faturamentoDia;
    private BigDecimal faturamentoMes;
    private BigDecimal faturamentoAno;
    private Long atendimentosHoje;
    private Long atendimentosMes;
    private BigDecimal ticketMedio;
    private BigDecimal growthPercentage;
    private Long totalClientes;
    private Long totalBarbeiros;
}

