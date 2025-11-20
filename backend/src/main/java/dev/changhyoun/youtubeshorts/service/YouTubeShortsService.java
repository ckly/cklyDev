package dev.changhyoun.youtubeshorts.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.changhyoun.youtubeshorts.dto.ShortVideoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YouTubeShortsService {

    @Value("${youtube.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<ShortVideoDto> getShortsRanking(String channelId, int limit) {
        try {
            List<String> videoIds = fetchVideoIds(channelId);

            if (videoIds.isEmpty()) {
                return new ArrayList<>();
            }

            List<ShortVideoDto> videos = fetchVideoDetails(videoIds);

            return videos.stream()
                    .sorted(Comparator.comparingLong(ShortVideoDto::getViewCount).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch YouTube shorts data", e);
        }
    }

    private List<String> fetchVideoIds(String channelId) throws Exception {
        String searchUrl = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/search")
                .queryParam("key", apiKey)
                .queryParam("channelId", channelId)
                .queryParam("part", "snippet")
                .queryParam("type", "video")
                .queryParam("maxResults", 50)
                .queryParam("order", "viewCount")
                .queryParam("videoDuration", "short")
                .queryParam("q", "#shorts")
                .build()
                .toUriString();

        String searchResponse = restTemplate.getForObject(searchUrl, String.class);
        JsonNode root = objectMapper.readTree(searchResponse);
        List<String> videoIds = new ArrayList<>();
        JsonNode items = root.path("items");
        if (items.isArray()) {
            for (JsonNode item : items) {
                String videoId = item.path("id").path("videoId").asText(null);
                if (videoId != null && !videoId.isEmpty()) {
                    videoIds.add(videoId);
                }
            }
        }
        return videoIds;
    }

    private List<ShortVideoDto> fetchVideoDetails(List<String> videoIds) throws Exception {
        String videoIdParam = String.join(",", videoIds);
        String videosUrl = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/videos")
                .queryParam("key", apiKey)
                .queryParam("id", videoIdParam)
                .queryParam("part", "snippet,statistics,contentDetails")
                .build()
                .toUriString();

        String videosResponse = restTemplate.getForObject(videosUrl, String.class);
        JsonNode root = objectMapper.readTree(videosResponse);
        List<ShortVideoDto> videos = new ArrayList<>();
        JsonNode items = root.path("items");
        if (items.isArray()) {
            for (JsonNode item : items) {
                String videoId = item.path("id").asText("");
                JsonNode snippet = item.path("snippet");
                JsonNode statistics = item.path("statistics");
                JsonNode contentDetails = item.path("contentDetails");

                String title = snippet.path("title").asText("");
                String channelTitle = snippet.path("channelTitle").asText("");
                String publishedAt = snippet.path("publishedAt").asText("");
                String thumbnailUrl = snippet.path("thumbnails").path("medium").path("url").asText("");
                long viewCount = statistics.path("viewCount").asLong(0);
                String durationIso = contentDetails.path("duration").asText("");

                videos.add(ShortVideoDto.builder()
                        .videoId(videoId)
                        .title(title)
                        .channelTitle(channelTitle)
                        .thumbnailUrl(thumbnailUrl)
                        .viewCount(viewCount)
                        .publishedAt(publishedAt)
                        .durationIso(durationIso)
                        .build());
            }
        }
        return videos;
    }
}
