package br.com.daniel.danbarbersaasapi.services;

import br.com.daniel.danbarbersaasapi.domain.order.OrderItem;
import br.com.daniel.danbarbersaasapi.domain.order.ServiceOrder;
import br.com.daniel.danbarbersaasapi.domain.report.*;
import br.com.daniel.danbarbersaasapi.repository.BarberRepository;
import br.com.daniel.danbarbersaasapi.repository.ClientRepository;
import br.com.daniel.danbarbersaasapi.repository.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ServiceOrderRepository serviceOrderRepository;
    private final BarberRepository barberRepository;
    private final ClientRepository clientRepository;

    public ReportResponseDTO getReportByPeriod(String period) {
        LocalDate today = LocalDate.now();
        OffsetDateTime startDate;
        OffsetDateTime endDate = today.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);

        switch (period != null ? period.toLowerCase() : "today") {
            case "yesterday":
                startDate = today.minusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
                endDate = today.minusDays(1).atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);
                break;
            case "week":
                startDate = today.with(DayOfWeek.MONDAY).atStartOfDay().atOffset(ZoneOffset.UTC);
                break;
            case "month":
                startDate = today.withDayOfMonth(1).atStartOfDay().atOffset(ZoneOffset.UTC);
                break;
            case "today":
            default:
                startDate = today.atStartOfDay().atOffset(ZoneOffset.UTC);
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

    public ComprehensiveReportDTO getComprehensiveReport(String period) {
        LocalDate today = LocalDate.now();
        OffsetDateTime startDate;
        OffsetDateTime endDate = today.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);

        switch (period != null ? period.toLowerCase() : "month") {
            case "week":
                startDate = today.with(DayOfWeek.MONDAY).atStartOfDay().atOffset(ZoneOffset.UTC);
                break;
            case "year":
                startDate = today.withDayOfYear(1).atStartOfDay().atOffset(ZoneOffset.UTC);
                break;
            case "month":
            default:
                startDate = today.withDayOfMonth(1).atStartOfDay().atOffset(ZoneOffset.UTC);
                break;
        }

        List<ServiceOrder> orders = serviceOrderRepository.findByCreatedAtBetween(startDate, endDate);

        // Summary
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

        // Barber Analysis
        List<BarberAnalysisDTO> barberAnalysis = getBarberAnalysis(startDate, endDate);

        // Top Clients
        List<ClientAnalysisDTO> topClients = getTopClients(startDate, endDate, 10);

        // Payment Methods
        List<PaymentMethodAnalysisDTO> paymentMethods = getPaymentMethodAnalysis(startDate, endDate, totalAmount);

        // Daily Trends
        List<DailyTrendDTO> dailyTrends = getDailyTrends(startDate, endDate);

        return ComprehensiveReportDTO.builder()
                .summary(summary)
                .barberAnalysis(barberAnalysis)
                .topClients(topClients)
                .paymentMethods(paymentMethods)
                .dailyTrends(dailyTrends)
                .period(period != null ? period : "month")
                .build();
    }

    private List<BarberAnalysisDTO> getBarberAnalysis(OffsetDateTime startDate, OffsetDateTime endDate) {
        return barberRepository.findAllActive().stream().map(barber -> {
            List<ServiceOrder> barberOrders = serviceOrderRepository.findByBarberIdAndCreatedAtBetween(
                    barber.getId(), startDate, endDate);

            BigDecimal totalRevenue = barberOrders.stream()
                    .map(ServiceOrder::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long totalServices = barberOrders.size();
            BigDecimal averageTicket = BigDecimal.ZERO;
            if (totalServices > 0) {
                averageTicket = totalRevenue.divide(new BigDecimal(totalServices), 2, RoundingMode.HALF_UP);
            }

            BigDecimal estimatedCommission = BigDecimal.ZERO;
            if (barber.getCommissionRate() != null) {
                estimatedCommission = totalRevenue.multiply(barber.getCommissionRate())
                        .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            }

            return BarberAnalysisDTO.builder()
                    .barberId(barber.getId())
                    .barberName(barber.getName())
                    .totalServices(totalServices)
                    .totalRevenue(totalRevenue)
                    .averageTicket(averageTicket)
                    .commissionRate(barber.getCommissionRate())
                    .estimatedCommission(estimatedCommission)
                    .build();
        }).sorted(Comparator.comparing(BarberAnalysisDTO::getTotalRevenue).reversed())
                .collect(Collectors.toList());
    }

    private List<ClientAnalysisDTO> getTopClients(OffsetDateTime startDate, OffsetDateTime endDate, int limit) {
        Map<UUID, List<ServiceOrder>> ordersByClient = serviceOrderRepository
                .findByCreatedAtBetween(startDate, endDate)
                .stream()
                .collect(Collectors.groupingBy(so -> so.getClient().getId()));

        return ordersByClient.entrySet().stream().map(entry -> {
            UUID clientId = entry.getKey();
            List<ServiceOrder> clientOrders = entry.getValue();

            BigDecimal totalSpent = clientOrders.stream()
                    .map(ServiceOrder::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long totalVisits = clientOrders.size();
            BigDecimal averageTicket = BigDecimal.ZERO;
            if (totalVisits > 0) {
                averageTicket = totalSpent.divide(new BigDecimal(totalVisits), 2, RoundingMode.HALF_UP);
            }

            String lastVisit = clientOrders.stream()
                    .map(so -> so.getCreatedAt().toLocalDate().toString())
                    .max(String::compareTo)
                    .orElse("N/A");

            String preferredBarber = clientOrders.stream()
                    .collect(Collectors.groupingBy(
                            so -> so.getBarber().getName(),
                            Collectors.counting()
                    ))
                    .entrySet()
                    .stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");

            return ClientAnalysisDTO.builder()
                    .clientId(clientId)
                    .clientName(clientOrders.get(0).getClient().getName())
                    .totalVisits(totalVisits)
                    .totalSpent(totalSpent)
                    .averageTicket(averageTicket)
                    .lastVisit(lastVisit)
                    .preferredBarber(preferredBarber)
                    .build();
        })
                .sorted(Comparator.comparing(ClientAnalysisDTO::getTotalSpent).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<PaymentMethodAnalysisDTO> getPaymentMethodAnalysis(OffsetDateTime startDate, OffsetDateTime endDate, BigDecimal totalAmount) {
        Map<String, List<ServiceOrder>> ordersByPaymentMethod = serviceOrderRepository
                .findByCreatedAtBetween(startDate, endDate)
                .stream()
                .filter(so -> so.getPaymentMethod() != null)
                .collect(Collectors.groupingBy(ServiceOrder::getPaymentMethod));

        return ordersByPaymentMethod.entrySet().stream().map(entry -> {
            String paymentMethod = entry.getKey();
            List<ServiceOrder> methodOrders = entry.getValue();

            BigDecimal methodAmount = methodOrders.stream()
                    .map(ServiceOrder::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            double percentage = 0;
            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                percentage = methodAmount.divide(totalAmount, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100))
                        .doubleValue();
            }

            return PaymentMethodAnalysisDTO.builder()
                    .paymentMethod(paymentMethod)
                    .totalTransactions((long) methodOrders.size())
                    .totalAmount(methodAmount)
                    .percentage(percentage)
                    .build();
        })
                .sorted(Comparator.comparing(PaymentMethodAnalysisDTO::getTotalAmount).reversed())
                .collect(Collectors.toList());
    }

    private List<DailyTrendDTO> getDailyTrends(OffsetDateTime startDate, OffsetDateTime endDate) {
        Map<LocalDate, List<ServiceOrder>> ordersByDate = serviceOrderRepository
                .findByCreatedAtBetween(startDate, endDate)
                .stream()
                .collect(Collectors.groupingBy(so -> so.getCreatedAt().toLocalDate()));

        return ordersByDate.entrySet().stream().map(entry -> {
            LocalDate date = entry.getKey();
            List<ServiceOrder> dateOrders = entry.getValue();

            BigDecimal revenue = dateOrders.stream()
                    .map(ServiceOrder::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long serviceCount = dateOrders.size();
            BigDecimal averageTicket = BigDecimal.ZERO;
            if (serviceCount > 0) {
                averageTicket = revenue.divide(new BigDecimal(serviceCount), 2, RoundingMode.HALF_UP);
            }

            return DailyTrendDTO.builder()
                    .date(date.toString())
                    .revenue(revenue)
                    .serviceCount(serviceCount)
                    .averageTicket(averageTicket)
                    .build();
        })
                .sorted(Comparator.comparing(DailyTrendDTO::getDate))
                .collect(Collectors.toList());
    }
}
