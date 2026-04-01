package com.statit.backend.controller;
import com.statit.backend.dto.ScoreResponse;
import com.statit.backend.dto.ScoreSubmitRequest;
import com.statit.backend.model.Score;
import com.statit.backend.service.ScoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/scores")
public class ScoreController {
    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @PatchMapping("/privacy")
    public ResponseEntity<Void> updatePrivacy(@RequestParam UUID userId, @RequestParam boolean anonymous) {
        scoreService.updateUserPrivacy(userId, anonymous);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<ScoreResponse> submitScore(@RequestBody ScoreSubmitRequest request) {
        Score newScore = scoreService.submitScore(
                request.userId(),
                request.categoryId(),
                request.score(),
                request.tags(),
                request.anonymous()
        );
        ScoreResponse response = ScoreResponse.fromScore(newScore, "Score submitted successfully");
        return ResponseEntity.ok(response);
    }
}
