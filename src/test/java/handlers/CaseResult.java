package handlers;

public record CaseResult(boolean passed, boolean skipped, String message) {

    public static CaseResult pass(String message) {
        return new CaseResult(true, false, message);
    }

    public static CaseResult fail(String message) {
        return new CaseResult(false, false, message);
    }

    public static CaseResult skip(String message) {
        return new CaseResult(false, true, message);
    }
}
