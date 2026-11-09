package com.notevault.repository;

import com.notevault.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Note entity persistence operations.
 * Enforces data privacy: every query is filtered by userId so a user can
 * only access their own notes.
 * Demonstrates Java Core concepts:
 * - Generics: Parameterized repository handling Note entity with Long ID.
 * - Collections: Returning List<Note> collections.
 * - Optional: Optional<Note> wrapping potential empty results.
 */
@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Note> findByIdAndUserId(Long id, Long userId);

    List<Note> findByUserIdAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(Long userId, String title);

    boolean existsByIdAndUserId(Long id, Long userId);
}
