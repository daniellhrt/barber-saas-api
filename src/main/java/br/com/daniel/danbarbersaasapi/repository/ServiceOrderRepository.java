package br.com.daniel.danbarbersaasapi.repository;

import br.com.daniel.danbarbersaasapi.domain.order.ServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, UUID> {

    List<ServiceOrder> findByClientIdOrderByCreatedAtDesc(UUID clientId);

    @Query("SELECT s FROM ServiceOrder s WHERE s.createdAt >= :startDate AND s.createdAt <= :endDate")
    List<ServiceOrder> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}