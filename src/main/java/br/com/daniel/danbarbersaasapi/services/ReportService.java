package br.com.daniel.danbarbersaasapi.services;

import br.com.daniel.danbarbersaasapi.domain.order.OrderItem;
import br.com.daniel.danbarbersaasapi.domain.order.ServiceOrder;
import br.com.daniel.danbarbersaasapi.domain.report.ReportOrderDTO;
import br.com.daniel.danbarbersaasapi.domain.report.ReportResponseDTO;
import br.com.daniel.danbarbersaasapi.domain.report.ReportSummaryDTO;
import br.com.daniel.danbarbersaasapi.repository.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ServiceOrderRepository serviceOrderRepository;

    public ReportResponseDTO getReportByPeriod(String period) {
        LocalDate today = LocalDate.now();
        LocalDateTime startDate;
        LocalDateTime endDate = today.atTime(LocalTime.MAX);

        switch (period != null ? period.toLowerCase() : "today") {
            case "yesterday":
                startDate = today.minusDays(1).atStartOfDay();
                endDate = today.minusDays(1).atTime(LocalTime.MAX);
                break;
            case "week":
                startDate = today.with(DayOfWeek.MONDAY).atStartOfDay();
                break;
            case "month":
                startDate = today.withDayOfMonth(1).atStartOfDay();
                break;
            case "today":
            default:
                startDate = today.atStartOfDay();
                break;
        }

        List<ServiceOrder> orders = serviceOrderRepository.findByCreatedAtBetween(startDate, endDate);

        BigDecimal totalAmount = orders.stream()
                .map(ServiceOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = orders.size();

        BigDecimal avgTicket = BigDecimal.ZERO;
        if (totalOrders > 0) {
            avgTicket = totalAmount.divide(new BigDecimal(totalOrders), 2, RoundingMode.HALF_UP);
        }

        ReportSummaryDTO summary = ReportSummaryDTO.builder()
                .totalAmount(totalAmount)
                .totalOrders(totalOrders)
                .avgTicket(avgTicket)
                .build();

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        List<ReportOrderDTO> reportOrders = orders.stream().map(order -> {
            String itemsStr = "N/A";
            // Check if there are items, then we need to map the items to string.
            // As OrderItem has referenceId and type, but the actual names might be fetched from services or products.
            // For now, let's just make it a simple string indicating quantity or type if no direct name is available in item
            // This is a simplification. Ideally, you should fetch names based on referenceId and type.
            
            return ReportOrderDTO.builder()
                    .id(order.getId())
                    .clientName(order.getClient() != null ? order.getClient().getName() : "N/A")
                    .barberName(order.getBarber() != null ? order.getBarber().getName() : "N/A")
                    .amount(order.getTotalAmount())
                    .items(itemsStr)
                    .time(order.getCreatedAt() != null ? order.getCreatedAt().format(timeFormatter) : "")
                    .payment(order.getPaymentMethod())
                    .build();
        }).collect(Collectors.toList());

        return ReportResponseDTO.builder()
                .summary(summary)
                .orders(reportOrders)
                .build();
    }
}
