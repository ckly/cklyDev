package dev.changhyoun.youtubeshorts.controller;

import dev.changhyoun.youtubeshorts.dto.ShortVideoDto;
import dev.changhyoun.youtubeshorts.service.YouTubeShortsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class ShortsRestController {

    private final YouTubeShortsService shortsService;

    @GetMapping("/shorts/rank")
    public List<ShortVideoDto> getShortsRanking(
            @RequestParam String channelId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return shortsService.getShortsRanking(channelId, limit);
    }
}
