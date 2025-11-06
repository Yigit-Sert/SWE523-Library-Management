package com.library.librarymanagement.repository;

import com.library.librarymanagement.model.BookRequest;
import com.library.librarymanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRequestRepository extends JpaRepository<BookRequest, Long> {

    List<BookRequest> findByRequestingUser(User user);

    @Query("SELECT br FROM BookRequest br JOIN FETCH br.requestingUser JOIN FETCH br.book ORDER BY br.requestDate DESC")
    List<BookRequest> findAllWithDetails();
}