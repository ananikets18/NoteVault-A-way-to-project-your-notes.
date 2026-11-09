package com.notevault.controller;

import com.notevault.dto.ApiResponse;
import com.notevault.dto.NoteRequest;
import com.notevault.dto.NoteResponse;
import com.notevault.security.CustomUserDetails;
import com.notevault.service.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller exposing REST APIs for personal note management.
 * Enforces data privacy so each authenticated user can only view,
 * create, update, search, or delete their own notes.
 */
@RestController
@RequestMapping("/api/notes")
@Tag(name = "Notes", description = "CRUD and search operations for personal notes")
@SecurityRequirement(name = "bearerAuth")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    /**
     * POST /api/notes - Create a new note
     */
    @PostMapping
    @Operation(summary = "Create a note", description = "Creates a new note associated with the authenticated user.")
    public ResponseEntity<ApiResponse<NoteResponse>> createNote(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody NoteRequest request) {

        NoteResponse createdNote = noteService.createNote(currentUser.getId(), request);
        return new ResponseEntity<>(
                ApiResponse.success("Note created successfully", createdNote),
                HttpStatus.CREATED
        );
    }

    /**
     * GET /api/notes - Get all notes OR search notes by title if search query is passed
     * Supports:
     * - GET /api/notes (all notes)
     * - GET /api/notes?search=java (search notes by title)
     */
    @GetMapping
    @Operation(summary = "Get all notes or search by title",
            description = "Retrieves all notes owned by the caller. If 'search' query parameter is provided, filters notes by title.")
    public ResponseEntity<ApiResponse<List<NoteResponse>>> getNotes(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Parameter(description = "Optional keyword to search notes by title")
            @RequestParam(value = "search", required = false) String search) {

        List<NoteResponse> notes;
        if (search != null && !search.trim().isEmpty()) {
            notes = noteService.searchNotes(currentUser.getId(), search);
        } else {
            notes = noteService.getAllNotes(currentUser.getId());
        }

        return ResponseEntity.ok(ApiResponse.success("Notes retrieved successfully", notes));
    }

    /**
     * GET /api/notes/{id} - Get a note by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get note by ID", description = "Fetches a single note owned by the caller using its ID.")
    public ResponseEntity<ApiResponse<NoteResponse>> getNoteById(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long id) {

        NoteResponse note = noteService.getNoteById(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Note retrieved successfully", note));
    }

    /**
     * PUT /api/notes/{id} - Update a note
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update note", description = "Edits an existing note owned by the caller.")
    public ResponseEntity<ApiResponse<NoteResponse>> updateNote(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long id,
            @Valid @RequestBody NoteRequest request) {

        NoteResponse updatedNote = noteService.updateNote(currentUser.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Note updated successfully", updatedNote));
    }

    /**
     * DELETE /api/notes/{id} - Delete a note
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete note", description = "Permanently deletes a note owned by the caller.")
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long id) {

        noteService.deleteNote(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Note deleted successfully", null));
    }
}
