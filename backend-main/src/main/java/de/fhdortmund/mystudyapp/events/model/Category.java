package de.fhdortmund.mystudyapp.events.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    /* ==================== PHASE 2 ADDITIONS ==================== */

    /** Icon identifier (e.g., "music", "tech", "sports") — maps to frontend SVG */
    @Column(name = "icon", length = 50)
    private String icon;

    /** Hex color for category badge background (e.g., "#FF5733") */
    @Column(name = "color", length = 7)
    private String color;

    /** Sort order for category list display */
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}