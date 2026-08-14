package com.minidoodle.schedular.meeting.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/** Meeting contact details; a participant is deliberately not a registered user account. */
public final class Participant {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9_+&*-]+(?:\\.[A-Za-z0-9_+&*-]+)*@" +
                    "(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$"
    );

    private final String name;
    private final String email;

    public Participant(String name, String email) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("email format is invalid: " + email);
        }
        this.name = name;
        this.email = email;
    }

    public String name() {
        return name;
    }

    public String email() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Participant that)) return false;
        return name.equals(that.name) && email.equals(that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email);
    }

    @Override
    public String toString() {
        return "Participant[" + name + ", " + email + "]";
    }
}
