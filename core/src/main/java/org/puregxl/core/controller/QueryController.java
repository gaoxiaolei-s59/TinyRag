package org.puregxl.core.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.puregxl.core.service.Rag.QueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Slf4j
@RequestMapping("/demo")
@RequiredArgsConstructor
public class QueryController {

    private final QueryService queryService;

    /**
     * 用户发起请求
     * @param message
     * @return
     */
    @PostMapping(value = "/query/v1")
    public ResponseEntity<String> query(@RequestBody String message) throws IOException {
        return ResponseEntity.ok(queryService.queryV1(message));
    }


}
