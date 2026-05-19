package br.com.daniel.danbarbersaasapi.domain.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprehensiveReportDTO {
    private ReportSummaryDTO summary;
    private List<BarberAnalysisDTO> barberAnalysis;
    private List<ClientAnalysisDTO> topClients;
    private List<PaymentMethodAnalysisDTO> paymentMethods;
    private List<DailyTrendDTO> dailyTrends;
    private String period;
}
