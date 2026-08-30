package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CareServiceAgreementResponse {
    Long id;
    Long requestId;
    Long renewalId;
    CareServiceAgreementType agreementType;
    Long memberId;
    Long doctorId;
    Long packageId;
    Long packageFamilyId;
    String packageCode;
    String packageName;
    Integer packageVersion;
    String serviceDescription;
    List<CareServiceCode> includedServices;
    List<CareServiceCode> excludedServices;
    BigDecimal priceAmount;
    String currency;
    Integer durationDays;
    Instant extensionStartsAt;
    Instant resultingEndsAt;
    CareStartRule startRule;
    String supportScheduleSnapshotJson;
    String supportTimezoneSnapshot;
    CareServiceSupportPolicy supportPolicy;
    Boolean renewable;
    String termsPolicyReference;
    String cancellationPolicyReference;
    String refundPolicyReference;
    String emergencyLimitation;
    String aiLimitation;
    String serviceLimitation;
    String healthDataScopeDisclosure;
    CareServiceAgreementStatus status;
    Long acceptedByMember;
    Instant acceptedAt;
    Instant validUntil;
    Instant invalidatedAt;
    String invalidationReason;
    Instant consumedAt;
    Instant createdAt;
}
