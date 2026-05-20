package de.fhdortmund.mystudyapp.events.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.fhdortmund.mystudyapp.common.response.ApiResponse;
import de.fhdortmund.mystudyapp.events.dto.SearchSuggestionDto;
import de.fhdortmund.mystudyapp.events.service.SearchService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<List<SearchSuggestionDto>>> getSuggestions(
            @RequestParam String q,
            @RequestParam(required = false, defaultValue = "ALL") String type) {

        List<SearchSuggestionDto> suggestions = searchService.getSuggestions(q, type);
        return ResponseEntity.ok(ApiResponse.success(suggestions, "Suggestions retrieved"));
    }
}