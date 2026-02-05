package com.fitness;

import com.fitness.util.PasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        String password = "admin123";
        String encoded = PasswordEncoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("Encoded: " + encoded);
        System.out.println("Matches: " + PasswordEncoder.matches(password, encoded));
    }
}
