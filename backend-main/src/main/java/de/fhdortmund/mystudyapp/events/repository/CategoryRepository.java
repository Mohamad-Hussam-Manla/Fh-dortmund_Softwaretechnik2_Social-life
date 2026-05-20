package de.fhdortmund.mystudyapp.events.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.fhdortmund.mystudyapp.events.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    boolean existsByName(String name);

    /* ==================== PHASE 2 ADDITIONS ==================== */

    List<Category> findTop5ByNameContainingIgnoreCase(String query, Pageable pageable);

    List<Category> findAllByOrderBySortOrderAsc();
}