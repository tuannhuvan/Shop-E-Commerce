package org.example.demofullstackspringboot.repository;

import org.example.demofullstackspringboot.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    boolean existsByName(String name);
    Optional<Category> findBySlug(String slug);
}
