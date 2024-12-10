package io.spring.examples;

import java.util.List;

public record AuthorInfo(
        String name,
        String primaryGenre,
        List<String> notableBooks,
        int yearsActive,
        String specialization
) {}
