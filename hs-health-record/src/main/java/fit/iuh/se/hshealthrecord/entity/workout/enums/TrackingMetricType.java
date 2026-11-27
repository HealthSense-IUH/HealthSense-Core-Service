package fit.iuh.se.hshealthrecord.entity.workout.enums;

public enum TrackingMetricType {
    TIME_CALORIES, // Thời gian và calo tiêu hao (không GPS)
    DISTANCE_GPS,  // Khoảng cách, tốc độ và lộ trình (có GPS)
    INDOOR_STEP,   // Đếm bước & quãng đường ước tính trong nhà (không GPS)
    SETS_REST      // Số hiệp và nghỉ ngơi
}
