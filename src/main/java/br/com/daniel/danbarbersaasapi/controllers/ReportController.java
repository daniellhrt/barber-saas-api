package br.com.daniel.danbarbersaasapi.controllers;

import br.com.daniel.danbarbersaasapi.domain.report.ComprehensiveReportDTO;
import br.com.daniel.danbarbersaasapi.domain.report.ReportResponseDTO;
import br.com.daniel.danbarbersaasapi.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    public ResponseEntity<ReportResponseDTO> getReports(@RequestParam(required = false, defaultValue = "today") String period) {
        return ResponseEntity.ok(reportService.getReportByPeriod(period));
    }

    @GetMapping("/comprehensive")
    public ResponseEntity<ComprehensiveReportDTO> getComprehensiveReport(@RequestParam(required = false, defaultValue = "month") String period) {
        return ResponseEntity.ok(reportService.getComprehensiveReport(period));
    }

    @GetMapping("/by-period")
    public ResponseEntity<ReportResponseDTO> getReportByCustomPeriod(@RequestParam String period) {
        return ResponseEntity.ok(reportService.getReportByPeriod(period));
    }
}
