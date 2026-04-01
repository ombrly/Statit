package com.statit.backend.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.UUID;

public record ScoreSubmitRequest(
        @JsonProperty("user_id") UUID userId,
        @JsonProperty("category_id") UUID categoryId,
        Float score,
        Map<String, String> tags,
        @JsonProperty("anonymous") Boolean anonymous
) {}
