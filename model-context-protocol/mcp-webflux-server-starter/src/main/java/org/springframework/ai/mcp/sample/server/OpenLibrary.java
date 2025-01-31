/*
* Copyright 2024 - 2024 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.springframework.ai.mcp.sample.server;

import java.util.List;
import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * @author Christian Tzolov
 */
@Service
public class OpenLibrary {

	private RestClient restClient;

	public OpenLibrary() {
		this(RestClient.builder());
	}

	public OpenLibrary(RestClient.Builder restClientBuilder) {
		this.restClient = restClientBuilder.baseUrl("https://openlibrary.org").build();
	}

	public record Books(Integer numFound, Integer start, Boolean numFoundExact, List<Map<String, Object>> docs) {
	}

	public record Book(List<String> isbn, String title, List<String> authorName) {
	}

	@Tool(description = "Get list of Books by title")
	public List<Book> getBooks(String title) {
		Books books = restClient.get()
			.uri(uriBuilder -> uriBuilder.path("/search.json").queryParam("q", title).build())
			.retrieve()
			.body(Books.class);
		if (books == null) {
			return List.of();
		}
		return books.docs.stream()
			.map(doc -> new Book((List<String>) doc.get("isbn"), (String) doc.get("title"),
					(List<String>) doc.get("author_name")))
			.toList();
	}

	@Tool(description = "Get book titles by author")
	public List<String> getBookTitlesByAuthor(String authorName) {
		var books = restClient.get()
			.uri(uriBuilder -> uriBuilder.path("/search/authors.json").queryParam("q", authorName).build())
			.retrieve()
			.body(Books.class);
		if (books == null) {
			return List.of();
		}
		return books.docs.stream().map(doc -> (String) doc.get("top_work")).toList();
	}

	public static void main(String[] args) {
		OpenLibrary openLibrary = new OpenLibrary(RestClient.builder());
		List<Book> books = openLibrary.getBooks("Spring Framework");
		System.out.println(books);
		List<String> booksByAuthor = openLibrary.getBookTitlesByAuthor("Craig Walls");
		System.out.println(booksByAuthor);
	}

}
