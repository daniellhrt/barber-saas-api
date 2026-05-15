package br.com.daniel.danbarbersaasapi.domain.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BarberAnalysisDTO {
    private UUID barberId;
    private String barberName;
    private Long totalServices;
    private BigDecimal totalRevenue;
    private BigDecimal averageTicket;
    private BigDecimal commissionRate;
    private BigDecimal estimatedCommission;
}

