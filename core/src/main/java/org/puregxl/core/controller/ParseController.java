package org.puregxl.core.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.puregxl.core.dto.ParseResult;
import org.puregxl.core.service.rag.DocumentIngestionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@Slf4j
@RequestMapping("/demo")
@RequiredArgsConstructor
public class ParseController {

    private final DocumentIngestionService documentIngestionService;

    /**
     * 解析传入的文档
     * @param file
     * @return
     */
    @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ParseResult> parse(@RequestParam("file") MultipartFile file) throws IOException {
        ParseResult parseResult = documentIngestionService.parseAndStore(file);
        if (!parseResult.isSuccess()) {
            return ResponseEntity.badRequest().body(parseResult);
        }
        return ResponseEntity.ok(parseResult);
    }


}
