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
public class ClientAnalysisDTO {
    private UUID clientId;
    private String clientName;
    private Long totalVisits;
    private BigDecimal totalSpent;
    private BigDecimal averageTicket;
    private String lastVisit;
    private String preferredBarber;
}

