package org.example.demofullstackspringboot.repository;

import org.example.demofullstackspringboot.entity.Order;
import org.example.demofullstackspringboot.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    @EntityGraph(attributePaths = {"customer", "items", "items.product"})
    @Query("SELECT o FROM Order o WHERE (:status IS NULL OR o.status = :status)")
    Page<Order> findOrdersWithFilter(@Param("status") OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"customer", "items", "items.product"})
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findOrderWithDetailsById(@Param("id") String id);

    @EntityGraph(attributePaths = {"customer", "items", "items.product"})
    List<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId);
}
