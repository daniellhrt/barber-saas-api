package br.com.daniel.danbarbersaasapi.domain.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDTO {
    private KpiDTO kpis;
    private List<ChartDataDTO> chartData;
}
