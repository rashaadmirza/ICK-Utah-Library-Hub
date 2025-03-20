package edu.northeastern.ickutah.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import edu.northeastern.ickutah.models.Book;

@Dao
public interface BookDao {
    @Insert
    void insert(Book book);

    @Query("SELECT * FROM books ORDER BY title ASC")
    List<Book> getAllBooks();

    @Query("SELECT * FROM books WHERE bookId = :bookId LIMIT 1")
    Book getBookById(String bookId);

    @Query("SELECT * FROM books WHERE LOWER(title) = LOWER(:title) AND LOWER(author) = LOWER(:author) LIMIT 1")
    Book getBookByTitleAndAuthor(String title, String author);

    @Update
    void update(Book book);

    @Delete
    void delete(Book book);

    // Home Page Queries
    @Query("SELECT COUNT(DISTINCT title) FROM books")
    int getUniqueTitleCount();

    @Query("SELECT SUM(totalCopies) FROM books")
    int getTotalBookCount(); // Sums up all copies of all books

    @Query("SELECT COUNT(DISTINCT author) FROM books")
    int getUniqueAuthorCount();

    // Search Queries
    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' OR publisher LIKE '%' || :query || '%' OR bookId LIKE '%' || :query || '%'")
    List<Book> searchBooks(String query);

    // Advanced
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<Book> books);
}