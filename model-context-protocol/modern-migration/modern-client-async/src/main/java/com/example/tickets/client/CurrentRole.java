package com.example.tickets.client;

import java.util.function.Supplier;

/**
 * The role of the caller that the current thread works for. It stands in for the security
 * context of a web application, where the caller is known on the thread that serves the
 * request.
 */
final class CurrentRole {

	private static final ThreadLocal<String> ROLE = new ThreadLocal<>();

	private CurrentRole() {
	}

	/** The role of the current caller, or {@code null} for a caller without one. */
	static String get() {
		return ROLE.get();
	}

	/** Runs the action as a caller with the given role. */
	static <T> T callAs(String role, Supplier<T> action) {
		ROLE.set(role);
		try {
			return action.get();
		}
		finally {
			ROLE.remove();
		}
	}

}
