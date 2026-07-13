package org.springframework.ai.rag.example.controller;

import org.springframework.ai.rag.example.service.OllamaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ai.rag.example.service.RAGService;

import java.io.IOException;

@RestController
public class RAGPgVectorController {

    @Autowired
    private RAGService ragService;
    @Autowired
    private OllamaService ollamaService;


    @PostMapping("/upload")
    public ResponseEntity<String> uploadFileInToDB(@RequestParam("file") MultipartFile file) throws IOException {
        ragService.processPDFFileIntoPgvectorDatabase(file);
        return ResponseEntity.ok("File uploaded successfully: " + file.getOriginalFilename());
    }
    @GetMapping("/chat")
    public ResponseEntity<String> chat(@RequestParam(defaultValue = "any news about usa") String message ) {
        String resp=ollamaService.callModelOnOllamaToGetRes(message);
        return ResponseEntity.ok(resp);
    }
}
