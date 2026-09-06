package fit.iuh.se.hschat.dto.response;

public record MinimalMemberIntakeContext(
        String reasonForCare,
        String currentConcern,
        String careGoal,
        String memberNote,
        String relevantSelfReportedContext) {
}
