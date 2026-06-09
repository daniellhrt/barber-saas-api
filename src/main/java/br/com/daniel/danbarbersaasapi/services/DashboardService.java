package br.com.daniel.danbarbersaasapi.services;

import br.com.daniel.danbarbersaasapi.domain.dashboard.AdvancedDashboardResponseDTO;
import br.com.daniel.danbarbersaasapi.domain.dashboard.AdvancedKpiDTO;
import br.com.daniel.danbarbersaasapi.domain.dashboard.ChartDataDTO;
import br.com.daniel.danbarbersaasapi.domain.dashboard.DashboardResponseDTO;
import br.com.daniel.danbarbersaasapi.domain.order.ServiceOrder;
import br.com.daniel.danbarbersaasapi.domain.report.BarberAnalysisDTO;
import br.com.daniel.danbarbersaasapi.domain.report.ClientAnalysisDTO;
import br.com.daniel.danbarbersaasapi.repository.BarberRepository;
import br.com.daniel.danbarbersaasapi.repository.ClientRepository;
import br.com.daniel.danbarbersaasapi.repository.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    @Value("${app.time-zone:America/Sao_Paulo}")
    private String dashboardZoneId;

    private ZoneId dashboardZone() {
        return ZoneId.of(dashboardZoneId);
    }

    private final ServiceOrderRepository serviceOrderRepository;
    private final BarberRepository barberRepository;
    private final ClientRepository clientRepository;
    private final ReportService reportService;

    public DashboardResponseDTO getDashboardData() {
        ZoneId zone = dashboardZone();
        LocalDate today = LocalDate.now(zone);
        OffsetDateTime startOfDay = today.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime endOfDay = today.atTime(LocalTime.MAX).atZone(zone).toOffsetDateTime();

        OffsetDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime endOfMonth = today.withDayOfMonth(today.lengthOfMonth()).atTime(LocalTime.MAX).atZone(zone).toOffsetDateTime();

        List<ServiceOrder> ordersToday = serviceOrderRepository.findByCreatedAtBetween(startOfDay, endOfDay);
        List<ServiceOrder> ordersMonth = serviceOrderRepository.findByCreatedAtBetween(startOfMonth, endOfMonth);

        BigDecimal faturamentoDia = ordersToday.stream()
                .map(ServiceOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal faturamentoMes = ordersMonth.stream()
                .map(ServiceOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long atendimentosHoje = ordersToday.size();

        BigDecimal ticketMedio = BigDecimal.ZERO;
        if (atendimentosHoje > 0) {
            ticketMedio = faturamentoDia.divide(new BigDecimal(atendimentosHoje), 2, RoundingMode.HALF_UP);
        }

        br.com.daniel.danbarbersaasapi.domain.dashboard.KpiDTO kpis = br.com.daniel.danbarbersaasapi.domain.dashboard.KpiDTO.builder()
                .faturamentoDia(faturamentoDia)
                .faturamentoMes(faturamentoMes)
                .atendimentosHoje(atendimentosHoje)
                .ticketMedio(ticketMedio)
                .build();

        List<ChartDataDTO> chartData = getChartData(today);

        return DashboardResponseDTO.builder()
                .kpis(kpis)
                .chartData(chartData)
                .build();
    }

    public AdvancedDashboardResponseDTO getAdvancedDashboardData() {
        ZoneId zone = dashboardZone();
        LocalDate today = LocalDate.now(zone);
        OffsetDateTime startOfDay = today.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime endOfDay = today.atTime(LocalTime.MAX).atZone(zone).toOffsetDateTime();

        OffsetDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime endOfMonth = today.withDayOfMonth(today.lengthOfMonth()).atTime(LocalTime.MAX).atZone(zone).toOffsetDateTime();

        OffsetDateTime startOfYear = today.withDayOfYear(1).atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime endOfYear = today.withDayOfYear(today.lengthOfYear()).atTime(LocalTime.MAX).atZone(zone).toOffsetDateTime();

        OffsetDateTime startOfLastMonth = today.minusMonths(1).withDayOfMonth(1).atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime endOfLastMonth = today.minusMonths(1).withDayOfMonth(today.minusMonths(1).lengthOfMonth()).atTime(LocalTime.MAX).atZone(zone).toOffsetDateTime();

        // Current period data
        List<ServiceOrder> ordersToday = serviceOrderRepository.findByCreatedAtBetween(startOfDay, endOfDay);
        List<ServiceOrder> ordersMonth = serviceOrderRepository.findByCreatedAtBetween(startOfMonth, endOfMonth);
        List<ServiceOrder> ordersYear = serviceOrderRepository.findByCreatedAtBetween(startOfYear, endOfYear);
        List<ServiceOrder> ordersLastMonth = serviceOrderRepository.findByCreatedAtBetween(startOfLastMonth, endOfLastMonth);

        BigDecimal faturamentoDia = ordersToday.stream()
                .map(ServiceOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal faturamentoMes = ordersMonth.stream()
                .map(ServiceOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal faturamentoAno = ordersYear.stream()
                .map(ServiceOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal faturamentoLastMonth = ordersLastMonth.stream()
                .map(ServiceOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long atendimentosHoje = ordersToday.size();
        long atendimentosMes = ordersMonth.size();

        BigDecimal ticketMedio = BigDecimal.ZERO;
        if (atendimentosHoje > 0) {
            ticketMedio = faturamentoDia.divide(new BigDecimal(atendimentosHoje), 2, RoundingMode.HALF_UP);
        }

        // Growth percentage
        BigDecimal growthPercentage = BigDecimal.ZERO;
        if (faturamentoLastMonth.compareTo(BigDecimal.ZERO) > 0) {
            growthPercentage = faturamentoMes.subtract(faturamentoLastMonth)
                    .divide(faturamentoLastMonth, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
        }

        long totalClientes = clientRepository.count();
        long totalBarbeiros = barberRepository.count();

        AdvancedKpiDTO kpis = AdvancedKpiDTO.builder()
                .faturamentoDia(faturamentoDia)
                .faturamentoMes(faturamentoMes)
                .faturamentoAno(faturamentoAno)
                .atendimentosHoje(atendimentosHoje)
                .atendimentosMes(atendimentosMes)
                .ticketMedio(ticketMedio)
                .growthPercentage(growthPercentage)
                .totalClientes(totalClientes)
                .totalBarbeiros(totalBarbeiros)
                .build();

        List<ChartDataDTO> chartData = getChartData(today);
        List<ChartDataDTO> monthlyComparisonData = getMonthlyComparisonData();

        // Get top performers
        List<BarberAnalysisDTO> topBarbers = reportService.getComprehensiveReport("month").getBarberAnalysis()
                .stream()
                .limit(5)
                .toList();

        List<ClientAnalysisDTO> topClients = reportService.getComprehensiveReport("month").getTopClients()
                .stream()
                .limit(5)
                .toList();

        return AdvancedDashboardResponseDTO.builder()
                .kpis(kpis)
                .chartData(chartData)
                .monthlyComparisonData(monthlyComparisonData)
                .topBarbers(topBarbers)
                .topClients(topClients)
                .lastUpdated(OffsetDateTime.now(ZoneOffset.UTC).toString())
                .build();
    }

    private List<ChartDataDTO> getChartData(LocalDate today) {
        ZoneId zone = dashboardZone();
        OffsetDateTime startOfWeek = today.with(DayOfWeek.MONDAY).atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime endOfWeek = today.with(DayOfWeek.SUNDAY).atTime(LocalTime.MAX).atZone(zone).toOffsetDateTime();

        List<ServiceOrder> ordersWeek = serviceOrderRepository.findByCreatedAtBetween(startOfWeek, endOfWeek);

        Map<DayOfWeek, BigDecimal> dailyTotals = ordersWeek.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getCreatedAt().getDayOfWeek(),
                        Collectors.mapping(ServiceOrder::getTotalAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        return List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
                .stream()
                .map(day -> {
                    String name = day.getDisplayName(TextStyle.SHORT, Locale.of("pt", "BR"));
                    // Capitalize first letter
                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                    return ChartDataDTO.builder()
                        .name(name)
                        .total(dailyTotals.getOrDefault(day, BigDecimal.ZERO))
                        .build();
                })
                .toList();
    }

    private List<ChartDataDTO> getMonthlyComparisonData() {
        ZoneId zone = dashboardZone();
        LocalDate today = LocalDate.now(zone);
        List<ChartDataDTO> monthlyData = new ArrayList<>();

        for (int i = 11; i >= 0; i--) {
            LocalDate monthStart = today.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

            OffsetDateTime startOfMonth = monthStart.atStartOfDay(zone).toOffsetDateTime();
            OffsetDateTime endOfMonth = monthEnd.atTime(LocalTime.MAX).atZone(zone).toOffsetDateTime();

            List<ServiceOrder> monthOrders = serviceOrderRepository.findByCreatedAtBetween(startOfMonth, endOfMonth);
            BigDecimal monthTotal = monthOrders.stream()
                    .map(ServiceOrder::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String monthName = monthStart.getMonth().getDisplayName(
                    java.time.format.TextStyle.SHORT,
                    Locale.of("pt", "BR")
            );

            monthlyData.add(ChartDataDTO.builder()
                    .name(monthName + "/" + monthStart.getYear())
                    .total(monthTotal)
                    .build());
        }

        return monthlyData;
    }
}
