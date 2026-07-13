package org.springframework.ai.rag.example.service;

import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


@Service
public class RAGService {

    private final VectorStore vectorStore;

    private static final Logger logger = Logger.getLogger(RAGService.class.getName());


    public RAGService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void processPDFFileIntoPgvectorDatabase(MultipartFile file) throws IOException {
        try {
            // Convert MultipartFile in ByteArrayResource
            ByteArrayResource resource = new ByteArrayResource(file.getBytes());
            // Create reader from ByteArrayResource file
            ParagraphPdfDocumentReader readMultipartFile = new ParagraphPdfDocumentReader(resource);
            // Split into smaller chunks by paragraph
            TokenTextSplitter textSplitter = new TokenTextSplitter();
            //  Load into vector store (assuming already configured)
            vectorStore.accept(textSplitter.apply(readMultipartFile.get()));
        } catch (Exception e) {
            logger.log(Level.WARNING,"Exception found while processing your file "+e.getMessage());
        }
    }
}
