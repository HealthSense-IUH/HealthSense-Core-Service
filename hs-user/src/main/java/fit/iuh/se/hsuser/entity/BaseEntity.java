package fit.iuh.se.hsuser.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    Instant updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    String updatedBy;
}
