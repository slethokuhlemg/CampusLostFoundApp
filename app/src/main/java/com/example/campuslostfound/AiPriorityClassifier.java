package com.example.campuslostfound;

import java.util.Locale;
import java.util.regex.Pattern;

public class AiPriorityClassifier {

    public static PriorityResult analyse(
            String category,
            String title,
            String description) {

        String safeCategory =
                category == null
                        ? ""
                        : category.trim();

        String safeTitle =
                title == null
                        ? ""
                        : title.trim();

        String safeDescription =
                description == null
                        ? ""
                        : description.trim();

        String text =
                (
                        safeCategory +
                                " " +
                                safeTitle +
                                " " +
                                safeDescription
                ).toLowerCase(Locale.ROOT);

        int score = 0;

        StringBuilder reason =
                new StringBuilder();

        /*
         * FIRST:
         * Use the selected category.
         *
         * This prevents a word such as "phone" inside
         * "headphones" from incorrectly changing the category.
         */

        if (safeCategory.equalsIgnoreCase("Laptop")) {

            score = 90;

            reason.append(
                    "Laptop/computer is a high-priority study device. "
            );
        }

        else if (safeCategory.equalsIgnoreCase("Cellphone")) {

            score = 80;

            reason.append(
                    "A cellphone is important for student communication and services. "
            );
        }

        else if (safeCategory.equalsIgnoreCase("Student Card")) {

            score = 70;

            reason.append(
                    "A student card is important for campus access and student services. "
            );
        }

        else if (safeCategory.equalsIgnoreCase("Tablet")) {

            score = 60;

            reason.append(
                    "A tablet can be an important study device. "
            );
        }

        else if (safeCategory.equalsIgnoreCase("Book")) {

            score = 50;

            reason.append(
                    "Study material can be important for academic work. "
            );
        }

        else if (safeCategory.equalsIgnoreCase("Calculator")) {

            score = 50;

            reason.append(
                    "A calculator can be important for coursework and tests. "
            );
        }

        else if (safeCategory.equalsIgnoreCase("Headphones")) {

            score = 40;

            reason.append(
                    "Headphones can be useful for studying and communication. "
            );
        }

        /*
         * If the category is Other or empty,
         * use keywords from the title and description.
         */

        else {

            if (containsAnyWholeWord(
                    text,
                    "laptop",
                    "computer",
                    "notebook"
            )) {

                score = 90;

                reason.append(
                        "The report appears to involve a laptop/computer, "
                                + "which is a high-priority study device. "
                );
            }

            else if (containsAnyWholeWord(
                    text,
                    "cellphone",
                    "cell phone",
                    "phone",
                    "smartphone"
            )) {

                score = 80;

                reason.append(
                        "The report appears to involve a cellphone, "
                                + "which is important for student communication and services. "
                );
            }

            else if (containsAnyWholeWord(
                    text,
                    "student card",
                    "student id",
                    "id card",
                    "student number card"
            )) {

                score = 70;

                reason.append(
                        "A student card is important for campus access and student services. "
                );
            }

            else if (containsAnyWholeWord(
                    text,
                    "tablet",
                    "ipad"
            )) {

                score = 60;

                reason.append(
                        "A tablet can be an important study device. "
                );
            }

            else if (containsAnyWholeWord(
                    text,
                    "book",
                    "textbook",
                    "study book",
                    "notes"
            )) {

                score = 50;

                reason.append(
                        "Study material can be important for academic work. "
                );
            }

            else if (containsAnyWholeWord(
                    text,
                    "calculator"
            )) {

                score = 50;

                reason.append(
                        "A calculator can be important for coursework and tests. "
                );
            }

            else if (containsAnyWholeWord(
                    text,
                    "headphones",
                    "earphones",
                    "earbuds"
            )) {

                score = 40;

                reason.append(
                        "Headphones can be useful for studying and communication. "
                );
            }

            else {

                score = 30;

                reason.append(
                        "The reported item does not match a higher-priority category. "
                );
            }
        }

        /*
         * Academic importance.
         */

        if (containsAnyWholeWord(
                text,
                "assignment",
                "exam",
                "test",
                "lecture",
                "class",
                "project",
                "study"
        )) {

            reason.append(
                    "The description indicates an academic need. "
            );
        }

        /*
         * Urgency.
         */

        if (containsAnyWholeWord(
                text,
                "urgent",
                "urgently",
                "tomorrow",
                "deadline"
        )) {

            reason.append(
                    "The student indicates urgency. "
            );
        }

        /*
         * Keep score between 0 and 100.
         */

        if (score > 100) {
            score = 100;
        }

        if (score < 0) {
            score = 0;
        }

        /*
         * Convert score into priority.
         */

        String priority;

        if (score >= 70) {

            priority = "HIGH";

        } else if (score >= 40) {

            priority = "MEDIUM";

        } else {

            priority = "LOW";
        }

        /*
         * Check whether enough information was provided.
         */

        boolean enoughInformation =
                !safeTitle.isEmpty() &&
                        !safeDescription.isEmpty() &&
                        !safeCategory.isEmpty();

        /*
         * Only HIGH-priority reports with enough information
         * can be automatically approved.
         *
         * "Other" still requires administrator review.
         */

        boolean autoApproved =
                priority.equals("HIGH") &&
                        score >= 70 &&
                        enoughInformation &&
                        !safeCategory.equalsIgnoreCase("Other");

        if (autoApproved) {

            reason.append(
                    "The report meets the requirements for automatic approval."
            );

        } else {

            reason.append(
                    "The report requires administrator review."
            );
        }

        return new PriorityResult(
                priority,
                score,
                autoApproved,
                reason.toString()
        );
    }

    /*
     * Checks for complete words instead of simple
     * text fragments.
     *
     * This means:
     *
     * "phone"     -> matches phone
     * "headphones" -> does NOT match phone
     */

    private static boolean containsAnyWholeWord(
            String text,
            String... words) {

        if (text == null || text.isEmpty()) {
            return false;
        }

        for (String word : words) {

            if (word == null || word.isEmpty()) {
                continue;
            }

            String pattern =
                    "(^|[^a-z0-9])"
                            + Pattern.quote(
                            word.toLowerCase(Locale.ROOT)
                    )
                            + "([^a-z0-9]|$)";

            if (Pattern
                    .compile(pattern)
                    .matcher(text)
                    .find()) {

                return true;
            }
        }

        return false;
    }
}