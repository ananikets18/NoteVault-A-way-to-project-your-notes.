package com.notevault.service;

import com.notevault.dto.NoteRequest;
import com.notevault.dto.NoteResponse;
import com.notevault.exception.ResourceNotFoundException;
import com.notevault.model.Note;
import com.notevault.model.User;
import com.notevault.repository.NoteRepository;
import com.notevault.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Service handling Notes business logic and enforcing strict user data privacy.
 * Demonstrates Java Core concepts:
 * - Streams & Lambdas: Mapping and filtering notes (LINQ Select/Where equivalent).
 * - Optional: Idiomatic handling of absent records.
 * - Exception Handling: Throwing targeted domain exceptions.
 */
@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public NoteService(NoteRepository noteRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new note belonging exclusively to the authenticated user.
     */
    @Transactional
    public NoteResponse createNote(Long userId, NoteRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Note note = new Note(user, request.getTitle().trim(), request.getContent().trim());
        Note savedNote = noteRepository.save(note);

        return mapToResponse(savedNote);
    }

    /**
     * Retrieves all notes owned by the authenticated user.
     * Demonstrates Java Streams mapping entities to DTOs.
     */
    @Transactional(readOnly = true)
    public List<NoteResponse> getAllNotes(Long userId) {
        List<Note> notes = noteRepository.findByUserIdOrderByCreatedAtDesc(userId);

        // Demonstrates Java Streams (equivalent to LINQ: notes.Select(n => toDto(n)).ToList())
        return notes.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Fetches a single note by ID, verifying that it belongs to the authenticated user.
     */
    @Transactional(readOnly = true)
    public NoteResponse getNoteById(Long userId, Long noteId) {
        Note note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", noteId));

        return mapToResponse(note);
    }

    /**
     * Updates an existing note owned by the authenticated user.
     */
    @Transactional
    public NoteResponse updateNote(Long userId, Long noteId, NoteRequest request) {
        Note note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", noteId));

        note.setTitle(request.getTitle().trim());
        note.setContent(request.getContent().trim());

        Note updatedNote = noteRepository.save(note);
        return mapToResponse(updatedNote);
    }

    /**
     * Deletes a note owned by the authenticated user.
     */
    @Transactional
    public void deleteNote(Long userId, Long noteId) {
        Note note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", noteId));

        noteRepository.delete(note);
    }

    /**
     * Searches notes by title for the authenticated user.
     * If query is empty, returns all user notes.
     * Demonstrates Java Streams filtering & sorting (LINQ Where / OrderBy equivalent).
     */
    @Transactional(readOnly = true)
    public List<NoteResponse> searchNotes(Long userId, String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllNotes(userId);
        }

        String trimmedQuery = query.trim().toLowerCase();

        // Query database repository
        List<Note> notes = noteRepository
                .findByUserIdAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(userId, trimmedQuery);

        // Process with Java Streams: map to DTOs and sort
        return notes.stream()
                .filter(note -> note.getTitle().toLowerCase().contains(trimmedQuery))
                .sorted(Comparator.comparing(Note::getCreatedAt).reversed())
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Maps Note domain entity to NoteResponse DTO.
     */
    private NoteResponse mapToResponse(Note note) {
        return new NoteResponse(
                note.getId(),
                note.getUser().getId(),
                note.getTitle(),
                note.getContent(),
                note.getCreatedAt()
        );
    }
}
