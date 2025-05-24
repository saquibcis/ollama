package com.example.demo.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class DocumentController {

    @Autowired
    private VectorStore vectorStore;

    /**
     * Uploads a PDF, DOCX, or TXT file. Parses its text and adds to Chroma vector DB.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return ResponseEntity.badRequest().body("Invalid file");
        }
        String text;
        try {
            String ext = StringUtils.getFilenameExtension(filename).toLowerCase();
            if ("pdf".equals(ext)) {
                // Parse PDF
                try (PDDocument pdf = PDDocument.load(file.getInputStream())) {
                    text = new PDFTextStripper().getText(pdf);
                }
            } else if ("docx".equals(ext) || "doc".equals(ext)) {
                // Parse Word
                try (XWPFDocument docx = new XWPFDocument(file.getInputStream());
                     XWPFWordExtractor extractor = new XWPFWordExtractor(docx)) {
                    text = extractor.getText();
                }
            } else if ("txt".equals(ext)) {
                // Parse text file
                text = new String(file.getBytes(), StandardCharsets.UTF_8);
            } else {
                return ResponseEntity.badRequest().body("Unsupported file type");
            }
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error parsing file: " + e.getMessage());
        }

//        // Create a Document with metadata and add to vector store:contentReference[oaicite:2]{index=2}
//        Map<String, Object> metadata = new HashMap<>();
//        metadata.put("source", filename);
//        Document doc = new Document(text, metadata);
//        vectorStore.add(Collections.singletonList(doc));
        TextSplitter splitter = new TokenTextSplitter(
                512,               // chunkSize (tokens)
                100,               // minChunkSizeChars
                50,                // minChunkLengthToEmbed
                Integer.MAX_VALUE, // maxNumChunks
                true               // keepSeparator
        );

        // Prepare metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", filename);

        // Split document and add to vector store
        List<Document> splitDocs = splitter.split(List.of(new Document(text, metadata)));
        vectorStore.add(splitDocs);


        return ResponseEntity.ok("File '" + filename + "' uploaded and processed");
    }

	
}
