package org.puregxl.core.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.puregxl.core.dto.ParseResult;
import org.puregxl.core.service.TikaParseService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Controller
@Slf4j
@RequestMapping("/demo")
@RequiredArgsConstructor
public class ParseController {

    private final TikaParseService tikaParseService;

    @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ParseResult> parse(MultipartFile file) {
        ParseResult parseResult = tikaParseService.parseFile(file);
        if (!parseResult.isSuccess()) {
            return ResponseEntity.ok(parseResult);
        } else {
            return ResponseEntity.badRequest().body(parseResult);
        }
    }

}
