package com.example.campuslostfound;

public class PriorityResult {
    public final String priority;
    public final int score;
    public final boolean autoApproved;
    public final String reason;

    public PriorityResult(String priority, int score, boolean autoApproved, String reason) {
        this.priority = priority;
        this.score = score;
        this.autoApproved = autoApproved;
        this.reason = reason;
    }
}
