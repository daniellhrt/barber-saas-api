package br.com.daniel.danbarbersaasapi.domain.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportSummaryDTO {
    private BigDecimal totalAmount;
    private Long totalOrders;
    private BigDecimal avgTicket;
}
