package edu.northeastern.ickutah.utils;

import android.util.Patterns;

public class StringUtils {

    /**
     * Converts a string to Title Case (e.g., "harry potter" -> "Harry Potter")
     */
    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder titleCase = new StringBuilder();
        boolean nextTitleCase = true;

        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            } else {
                c = Character.toLowerCase(c);
            }
            titleCase.append(c);
        }
        return titleCase.toString();
    }

    /**
     * Converts a string to lower case (e.g., "Johndoe@email.com" -> "johndoe@email.com")
     */
    public static String toLowerCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder lowerCase = new StringBuilder();
        boolean nextLowerCase = true;

        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextLowerCase = true;
            } else if (nextLowerCase) {
                c = Character.toLowerCase(c);
                nextLowerCase = false;
            } else {
                c = Character.toLowerCase(c);
            }
            lowerCase.append(c);
        }
        return lowerCase.toString();
    }

    /**
     * Validate Email Format
     */
    public static boolean isValidEmail(String email) {
        return email == null || !Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validate Phone Number (10 Digits)
     */
    public static boolean isValidPhone(String phone) {
        return phone == null || !phone.matches("^[0-9]{10}$"); // Accepts only 10-digit numeric values
    }
}