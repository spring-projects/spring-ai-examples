package com.vectors.ai;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class RagService {

    private static final Logger logger = LoggerFactory.getLogger(RagService.class);

    @Autowired
    private VectorStore vectorStore;

    @Value("${app.rag.pdf.folderPath}")
    private String pdfFolderPath; 
    private Optional<JdbcTemplate> nativeClient;
    private JdbcTemplate jdbc;
    @Value("${spring.ai.embedding.chunk-size}")
    private Integer chunkSize;
    private static String checkIfFileExistQuery="SELECT COUNT(*) FROM vector_store WHERE JSON_UNQUOTE(JSON_EXTRACT(metadata, '$.source')) = ?";

    @PostConstruct
    public void init() throws URISyntaxException {
    //    loadSampleDocuments();
    this.nativeClient = vectorStore.getNativeClient();
    if (this.nativeClient.isPresent()) {
        this.jdbc = nativeClient.get();
        // Use the native client for MariaDB-specific operations
    } 
    loadPDFEmbeddings();

    }
    // Loading example files to the vector search
    public void loadSampleDocuments() {
        List<Document> documents = List.of(
                new Document("Spring AI rocks Spring AI rocks Spring AI rocks Spring AI rocks Spring AI rocks", Map.of("source", "sample-doc-1")),
                new Document("The World is Big and Salvation Lurks Around the Corner", Map.of("source", "sample-doc-2")),
                new Document("You walk forward facing the past and you turn back toward the future.", Map.of("source", "sample-doc-3")));
        // Add the documents to MariaDB
        vectorStore.add(documents);
        logger.info("Loaded {} sample documents into the vector store.", documents.size());
    }
    // Loading PDFs from a folder 
    // TO-DO: Implement text splitter to reduce the content stored on each record
    public void loadPDFEmbeddings() throws URISyntaxException {
        logger.info("Attempting to load PDF documents from folder: {}", pdfFolderPath);
        
        URI uri = RagService.class.getClassLoader().getResource(pdfFolderPath).toURI();

        Path folderPath = Paths.get(uri);

        if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
            logger.warn("PDF folder path does not exist or is not a directory: {}", pdfFolderPath);
            return;
        }

        List<Document> pdfDocuments = new ArrayList<>();
        List<Document> splittedDocuments = new ArrayList<>();
        TokenTextSplitter textSplitter = TokenTextSplitter.builder()
                .withChunkSize(chunkSize)
                .build();

        logger.info("Using TokenTextSplitter with chunkSize={}", chunkSize);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath, "*.pdf")) {
            for (Path pdfFile : stream) {
                    // Checking if file was processed already               
                    Integer count = jdbc.queryForObject(checkIfFileExistQuery, Integer.class, pdfFile.getFileName().toString());

                    if (count != null && count > 0) {
                        logger.info("PDF already processed and found in vector store, skipping: {}", pdfFile.getFileName().toString());
                        continue; // Skip to the next file
                    }

                try (PDDocument document = Loader.loadPDF(pdfFile.toFile())) {
                     
                    PDFTextStripper pdfStripper = new PDFTextStripper();
                    String text = pdfStripper.getText(document);
                    // You can add more metadata if needed, e.g., last modified date, etc.
                    Map<String, Object> metadata = Map.of("source", pdfFile.getFileName().toString(), "filePath", pdfFile.toString());
                    pdfDocuments.add(new Document(text, metadata));
                    logger.info("Successfully parsed and created document for: {}", pdfFile.getFileName());
                } catch (IOException e) {
                    logger.error("Error processing PDF file {}: {}", pdfFile.getFileName(), e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            logger.error("Error reading PDF directory {}: {}", pdfFolderPath, e.getMessage(), e);
            return;
        }

        if (!pdfDocuments.isEmpty()) {
            logger.info("Splitting {} PDF documents into chunks...", pdfDocuments.size());
            splittedDocuments = textSplitter.split(pdfDocuments);
            vectorStore.add(splittedDocuments);
            logger.info("Successfully loaded and added {} PDF documents to the vector store.", splittedDocuments.size());
        } else {
            logger.info("No PDF documents found or processed in folder: {}", pdfFolderPath);
        }
    }

    public List<Document> getEmbeddings(String query) {
        // Retrieve documents similar to a query
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(2) // Consider making topK configurable
                .build();
        List<Document> results = vectorStore.similaritySearch(searchRequest);
        logger.info("Found {} documents for query '{}': {}", results.size(), query, results);
        return results;
    }
}
