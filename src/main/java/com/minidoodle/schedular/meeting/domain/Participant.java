package com.minidoodle.schedular.meeting.domain;

import java.util.Objects;

public final class Participant {

    private final String name;
    private final String email;

    public Participant(String name, String email) {
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
