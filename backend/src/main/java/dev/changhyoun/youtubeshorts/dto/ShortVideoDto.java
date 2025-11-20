package dev.changhyoun.youtubeshorts.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShortVideoDto {
    private String videoId;
    private String title;
    private String channelTitle;
    private String thumbnailUrl;
    private long viewCount;
    private String publishedAt;
    private String durationIso;
}
