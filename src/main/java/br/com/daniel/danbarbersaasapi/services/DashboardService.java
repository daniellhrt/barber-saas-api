package br.com.daniel.danbarbersaasapi.services;

import br.com.daniel.danbarbersaasapi.domain.dashboard.ChartDataDTO;
import br.com.daniel.danbarbersaasapi.domain.dashboard.DashboardResponseDTO;
import br.com.daniel.danbarbersaasapi.domain.dashboard.KpiDTO;
import br.com.daniel.danbarbersaasapi.domain.order.ServiceOrder;
import br.com.daniel.danbarbersaasapi.repository.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ServiceOrderRepository serviceOrderRepository;

    public DashboardResponseDTO getDashboardData() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = today.withDayOfMonth(today.lengthOfMonth()).atTime(LocalTime.MAX);

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

        KpiDTO kpis = KpiDTO.builder()
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

    private List<ChartDataDTO> getChartData(LocalDate today) {
        LocalDateTime startOfWeek = today.with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime endOfWeek = today.with(DayOfWeek.SUNDAY).atTime(LocalTime.MAX);

        List<ServiceOrder> ordersWeek = serviceOrderRepository.findByCreatedAtBetween(startOfWeek, endOfWeek);

        Map<DayOfWeek, BigDecimal> dailyTotals = ordersWeek.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getCreatedAt().getDayOfWeek(),
                        Collectors.mapping(ServiceOrder::getTotalAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        return List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
                .stream()
                .map(day -> {
                    String name = day.getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"));
                    // Capitalize first letter
                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                    return ChartDataDTO.builder()
                        .name(name)
                        .total(dailyTotals.getOrDefault(day, BigDecimal.ZERO))
                        .build();
                })
                .collect(Collectors.toList());
    }
}