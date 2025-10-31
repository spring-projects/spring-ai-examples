/* 
* Copyright 2025 - 2025 the original author or authors.
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
package org.springframework.ai.mcp.sample.server.providers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;

import org.springframework.stereotype.Service;

/**
 * @author Christian Tzolov
 */
@Service
public class DocumentProvider {

	private static final String DOCS_DIR = Paths.get(
        System.getProperty("user.dir"), "docs"
    ).toString();

	public static String getDocPath(String docId) {
		return Paths.get(DOCS_DIR, docId).toString();    
	}

	@McpTool(description = "Read the contents of a document and return it as a string.", name = "read_doc_contents")
	public String readDocContents(@McpToolParam String docId) {
        Path docPath = Paths.get(getDocPath(docId));
        if (!Files.exists(docPath)) {
            throw new IllegalArgumentException("Doc with id " + docId + " not found");
        }

		try {
        	return Files.readString(docPath);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to read the document " + docId, ex);
		}
	}	
	
	@McpTool(description = "Edit a document by replacing a string in the documents content with a new string", name = "edit_document")
	public void editDocument(@McpToolParam String docId, 
		@McpToolParam String oldContent, 
		@McpToolParam String newContent) {

        Path docPath = Paths.get(getDocPath(docId));
        if (!Files.exists(docPath)) {
            throw new IllegalArgumentException("Doc with id " + docId + " not found");
        }

		try {
        String content = Files.readString(docPath);
        content = content.replace(oldContent, newContent);
        Files.writeString(docPath, content);

		} catch (Exception ex) {
			throw new RuntimeException("Failed to edit the document " + docId, ex);
		}
	}	
	
	@McpResource(uri = "docs://documents", mimeType = "application/json")
	public List<String> listDocs() {

		try (Stream<Path> stream = Files.list(Paths.get(DOCS_DIR))) {
            return stream
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toList());
        } catch( Exception ex) {
			throw new RuntimeException("Failed to list documents", ex);
		}
	}

	@McpResource(uri = "docs://documents/{docId}", mimeType = "text/plain")
	public String fetchDoc(String docId) {
        Path docPath = Paths.get(getDocPath(docId));
        if (!Files.exists(docPath)) {
            throw new IllegalArgumentException("Doc with id " + docId + " not found");
        }
        try {
			return Files.readString(docPath);
		} catch (IOException e) {
			throw new RuntimeException("Failed to read the document " + docId, e);
		}
	}

	@McpPrompt(name = "format", description = "Rewrites the contents of the document in Markdown format.")
	public PromptMessage format(@McpArg(name = "docId", required = true) String docId) {

	    var prompt = """
			Your goal is to reformat a document to be written with markdown syntax.

			The id of the document you need to reformat is:
			<document_id>
			%s
			</document_id>

			Add in headers, bullet points, tables, etc as necessary. Feel free to add in extra text, but don't change the meaning of the report.
			Use the 'edit_document' tool to edit the document. After the document has been edited, respond with the final version of the doc. Don't explain your changes.
			""".formatted(docId);			

		return new PromptMessage(Role.USER, new TextContent(prompt));

	}




}
