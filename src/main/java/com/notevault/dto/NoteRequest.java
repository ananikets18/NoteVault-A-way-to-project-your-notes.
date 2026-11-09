package com.notevault.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating or updating a Note.
 */
@Schema(description = "Note creation or update payload")
public class NoteRequest {

    @Schema(description = "Title of the note (max 100 characters)", example = "Spring Boot Best Practices")
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    @Schema(description = "Content of the note", example = "Remember to always use DTOs and handle exceptions globally.")
    @NotBlank(message = "Content is required")
    private String content;

    public NoteRequest() {
    }

    public NoteRequest(String title, String content) {
        this.title = title;
        this.content = content;
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
}
