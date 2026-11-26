package nxp.student;

public record Subj(String code, String title, Integer mid, Integer fin) {
    public Integer overall() { return mid == null || fin == null ? null : (int) Math.round((mid + fin) / 2.0); }
}
