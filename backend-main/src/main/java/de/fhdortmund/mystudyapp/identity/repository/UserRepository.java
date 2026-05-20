package de.fhdortmund.mystudyapp.identity.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.fhdortmund.mystudyapp.identity.model.TrustLevel;
import de.fhdortmund.mystudyapp.identity.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUniversityEmail(String universityEmail);

    boolean existsByUniversityEmail(String universityEmail);

    Page<User> findByTrustLevel(TrustLevel trustLevel, Pageable pageable);

    @Modifying
    @Query("UPDATE User u SET u.trustLevel = :trustLevel WHERE u.id = :userId")
    void updateTrustLevel(@Param("userId") UUID userId, @Param("trustLevel") TrustLevel trustLevel);

    @Query("SELECT u FROM User u WHERE LOWER(u.displayName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(u.universityEmail) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<User> searchByDisplayNameOrEmail(@Param("query") String query, Pageable pageable);

    @Query("SELECT u FROM User u WHERE (LOWER(u.displayName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(u.universityEmail) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND u.trustLevel = :trustLevel")
    Page<User> searchByDisplayNameOrEmailAndTrustLevel(
            @Param("query") String query,
            @Param("trustLevel") TrustLevel trustLevel,
            Pageable pageable);

    /* ==================== PHASE 2 ADDITIONS ==================== */

    long countByCreatedAtAfter(Instant createdAt);

    @Query("SELECT u FROM User u WHERE LOWER(u.displayName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> findTop5ByDisplayNameContainingIgnoreCase(@Param("query") String query, Pageable pageable);
}