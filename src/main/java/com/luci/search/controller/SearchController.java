package com.luci.search.controller;

import com.luci.search.model.SearchResult;
import com.luci.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
    public SearchResult search(@RequestParam String query) {
        return searchService.searchWikipedia(query);
    }
}
