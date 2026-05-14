package services;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class BadWordFilter {

    private static final List<String> BAD_WORDS = Arrays.asList(
            "idiot", "stupid", "hate", "kill", "noob",
            "loser", "trash", "moron", "dumb"
    );

    public String filter(String text) {
        String result = text;
        for (String word : BAD_WORDS) {
            String stars = "*".repeat(word.length());
            result = result.replaceAll("(?i)\\b" + Pattern.quote(word) + "\\b", stars);
        }
        return result;
    }

    public boolean contains(String text) {
        String lower = text.toLowerCase();
        return BAD_WORDS.stream().anyMatch(lower::contains);
    }

    public boolean shouldHide(String text) {
        long count = BAD_WORDS.stream()
                .filter(w -> text.toLowerCase().contains(w))
                .count();
        return count >= 2;
    }
}