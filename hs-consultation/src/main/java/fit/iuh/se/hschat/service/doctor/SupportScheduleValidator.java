package fit.iuh.se.hschat.service.doctor;

public interface SupportScheduleValidator {

    void validate(String availabilityJson, String timezone, boolean required);

    boolean isValid(String availabilityJson, String timezone, boolean required);
}
