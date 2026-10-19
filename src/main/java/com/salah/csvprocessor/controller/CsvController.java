package com.salah.csvprocessor.controller;

import com.salah.csvprocessor.service.CsvService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class CsvController {
    private final CsvService csvService;

    public CsvController(CsvService csvService) {
        this.csvService = csvService;
    }

    @RequestMapping("/upload")
    public ResponseEntity<?> processCsv(@RequestBody MultipartFile file) throws IOException {
        return csvService.processCsv(file);
    }
}
