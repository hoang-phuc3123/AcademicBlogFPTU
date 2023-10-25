package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.*;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class ImageUploadController {

    @PostMapping("/image-upload")
    public ResponseEntity<Map<String, String>> uploadImages(@RequestParam("file[]") List<MultipartFile> files) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String phpApiUrl = "https://lvnsoft.store/upload.php";
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        for (MultipartFile file : files) {
            body.add("file[]", file.getResource());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(phpApiUrl, HttpMethod.POST, requestEntity, String.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> jsonDataList = objectMapper.readValue(responseEntity.getBody(), new TypeReference<List<Map<String, Object>>>() {});
            String link = "";
            for (Map<String, Object> jsonData : jsonDataList) {
                link = (String) jsonData.get("link");
            }
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("link", link);
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "File upload failed.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
