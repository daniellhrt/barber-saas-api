package br.com.daniel.danbarbersaasapi.infra.util;

import java.time.*;

/**
 * Utilitário centralizado para cálculo de períodos de data.
 * Elimina a duplicação de switch/case entre DashboardService e ReportService.
 */
public final class DateRangeHelper {

    private DateRangeHelper() {
        // utility class
    }

    /**
     * Calcula startDate e endDate para um dado período textual.
     *
     * @param period "today", "yesterday", "week", "month", "year"
     * @param zone   fuso horário para cálculos
     * @return par [startDate, endDate] como OffsetDateTime
     */
    public static OffsetDateTime[] resolve(String period, ZoneId zone) {
        LocalDate today = LocalDate.now(zone);
        OffsetDateTime startDate;
        OffsetDateTime endDate;

        switch (period != null ? period.toLowerCase() : "today") {
            case "yesterday":
                startDate = today.minusDays(1).atStartOfDay(zone).toOffsetDateTime();
                endDate = today.minusDays(1).atTime(LocalTime.MAX).atZone(zone).toOffsetDateTime();
                break;
            case "week":
                startDate = today.with(DayOfWeek.MONDAY).atStartOfDay(zone).toOffsetDateTime();
                endDate = today.atTime(LocalTime.MAX).atZone(zone).toOffsetDateTime();
                break;
            case "month":
                startDate = today.withDayOfMonth(1).atStartOfDay(zone).toOffsetDateTime();
                endDate = today.atTime(LocalTime.MAX).atZone(zone).toOffsetDateTime();
                break;
            case "year":
                startDate = today.withDayOfYear(1).atStartOfDay(zone).toOffsetDateTime();
                endDate = today.atTime(LocalTime.MAX).atZone(zone).toOffsetDateTime();
                break;
            case "today":
            default:
                startDate = today.atStartOfDay(zone).toOffsetDateTime();
                endDate = today.atTime(LocalTime.MAX).atZone(zone).toOffsetDateTime();
                break;
        }

        return new OffsetDateTime[]{startDate, endDate};
    }
}
