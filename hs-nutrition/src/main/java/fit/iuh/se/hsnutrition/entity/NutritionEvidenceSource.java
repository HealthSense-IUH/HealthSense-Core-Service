package fit.iuh.se.hsnutrition.entity;

import fit.iuh.se.hsnutrition.entity.enums.EvidenceSourceType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "nutrition_evidence_sources")
@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NutritionEvidenceSource {
    @Id
    @Column(length = 60)
    String id;
    @Column(nullable = false, length = 500)
    String title;
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    EvidenceSourceType sourceType;
    @Column(length = 300)
    String authors;
    @Column(length = 200)
    String journal;
    @Column(name = "publication_year")
    Integer publicationYear;
    @Column(length = 500)
    String url;
    @Column(length = 1000)
    String summary;
}
