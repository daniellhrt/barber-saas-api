package br.com.daniel.danbarbersaasapi.domain.dashboard;

import br.com.daniel.danbarbersaasapi.domain.report.BarberAnalysisDTO;
import br.com.daniel.danbarbersaasapi.domain.report.ClientAnalysisDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedDashboardResponseDTO {
    private AdvancedKpiDTO kpis;
    private List<ChartDataDTO> chartData;
    private List<ChartDataDTO> monthlyComparisonData;
    private List<BarberAnalysisDTO> topBarbers;
    private List<ClientAnalysisDTO> topClients;
    private String lastUpdated;
}

