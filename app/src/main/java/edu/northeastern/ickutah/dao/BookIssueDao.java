package edu.northeastern.ickutah.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import edu.northeastern.ickutah.models.BookIssue;

@Dao
public interface BookIssueDao {
    @Insert
    void insert(BookIssue issue);

    @Update
    void update(BookIssue issue);

    @Query("SELECT * FROM book_issues")
    List<BookIssue> getAllBookIssues();

    @Query("SELECT * FROM book_issues WHERE isReturned = 0")
    List<BookIssue> getIssuedBooks();

    @Query("SELECT * FROM book_issues WHERE issueId = :issueId LIMIT 1")
    BookIssue getIssueById(String issueId);


    @Query("SELECT COUNT(*) FROM book_issues WHERE bookId = :bookId AND isReturned = 0")
    int getIssuedCopiesCount(String bookId); // Method to count how many copies are already issued

    @Query("SELECT COUNT(*) FROM book_issues WHERE readerId = :readerId AND isReturned = 0")
    int getCurrentCheckouts(String readerId);  // Method to Get Current Checkouts Count for a Reader

    @Query("SELECT * FROM book_issues WHERE isReturned = :showReturned OR isReturned = 0 ORDER BY issueDate DESC")
    List<BookIssue> getFilteredBookIssues(boolean showReturned);

    @Query("UPDATE book_issues SET readerId = :newReaderId WHERE readerId = :oldReaderId AND isReturned = 0")
    void updateReaderIdInBookIssues(String oldReaderId, String newReaderId);

    // Home Page Queries
    @Query("SELECT COUNT(DISTINCT readerId) FROM book_issues WHERE isReturned = 0")
    int getCurrentlyReadingCount();

    @Query("SELECT COUNT(*) FROM book_issues WHERE isReturned = 0")
    int getTotalIssuedBooks();

    @Query("SELECT COUNT(*) FROM book_issues WHERE isReturned = 0 AND dueDate < :currentDate")
    int getOverdueBooksCount(long currentDate);

    // Search Queries
    @Query("SELECT * FROM book_issues WHERE issueId LIKE '%' || :query || '%' OR bookId LIKE '%' || :query || '%' OR readerId LIKE '%' || :query || '%'")
    List<BookIssue> searchBookIssues(String query);

    // Advanced
    @Query("DELETE FROM book_issues WHERE isReturned = 1")
    int deleteReturnedIssues();
}