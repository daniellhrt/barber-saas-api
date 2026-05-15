package br.com.daniel.danbarbersaasapi.controllers;

import br.com.daniel.danbarbersaasapi.domain.dashboard.AdvancedDashboardResponseDTO;
import br.com.daniel.danbarbersaasapi.domain.dashboard.DashboardResponseDTO;
import br.com.daniel.danbarbersaasapi.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardResponseDTO> getDashboardData() {
        return ResponseEntity.ok(dashboardService.getDashboardData());
    }

    @GetMapping("/advanced")
    public ResponseEntity<AdvancedDashboardResponseDTO> getAdvancedDashboardData() {
        return ResponseEntity.ok(dashboardService.getAdvancedDashboardData());
    }
}
