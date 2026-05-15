package br.com.daniel.danbarbersaasapi.repository;

import br.com.daniel.danbarbersaasapi.domain.order.ServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, UUID> {

    List<ServiceOrder> findByClientIdOrderByCreatedAtDesc(UUID clientId);

    @Query("SELECT s FROM ServiceOrder s WHERE s.createdAt >= :startDate AND s.createdAt <= :endDate")
    List<ServiceOrder> findByCreatedAtBetween(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT s FROM ServiceOrder s WHERE s.barber.id = :barberId AND s.createdAt >= :startDate AND s.createdAt <= :endDate")
    List<ServiceOrder> findByBarberIdAndCreatedAtBetween(@Param("barberId") UUID barberId, @Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT s FROM ServiceOrder s WHERE s.client.id = :clientId AND s.createdAt >= :startDate AND s.createdAt <= :endDate")
    List<ServiceOrder> findByClientIdAndCreatedAtBetween(@Param("clientId") UUID clientId, @Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT s FROM ServiceOrder s WHERE s.paymentMethod = :paymentMethod AND s.createdAt >= :startDate AND s.createdAt <= :endDate")
    List<ServiceOrder> findByPaymentMethodAndCreatedAtBetween(@Param("paymentMethod") String paymentMethod, @Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT s FROM ServiceOrder s WHERE s.createdAt >= :startDate AND s.createdAt <= :endDate ORDER BY s.totalAmount DESC")
    List<ServiceOrder> findTopOrdersByAmount(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);
}