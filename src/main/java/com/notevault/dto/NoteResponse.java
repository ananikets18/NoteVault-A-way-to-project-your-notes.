package com.notevault.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing note details returned to clients.
 */
@Schema(description = "Note response object")
public class NoteResponse {

    @Schema(description = "Unique note ID", example = "101")
    private Long id;

    @Schema(description = "Owner user ID", example = "1")
    private Long userId;

    @Schema(description = "Note title", example = "Spring Boot Best Practices")
    private String title;

    @Schema(description = "Note content", example = "Remember to always use DTOs and handle exceptions globally.")
    private String content;

    @Schema(description = "Creation timestamp", example = "2026-11-09T08:30:00")
    private LocalDateTime createdAt;

    public NoteResponse() {
    }

    public NoteResponse(Long id, Long userId, String title, String content, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
