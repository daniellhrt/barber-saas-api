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
public class ReportOrderDTO {
    private UUID id;
    private String clientName;
    private String barberName;
    private BigDecimal amount;
    private String items;
    private String time;
    private String payment;
}
