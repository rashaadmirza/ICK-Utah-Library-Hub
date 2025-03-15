package edu.northeastern.ickutah.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import java.io.Serializable;
import java.util.Date;
import edu.northeastern.ickutah.database.DateConverter;

@Entity(tableName = "book_issues")
@TypeConverters(DateConverter.class)
public class BookIssue implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private final String issueId;
    private final String bookId;
    private final String readerId;
    private final Date issueDate;
    private final Date dueDate;
    private Date returnDate;
    private boolean isReturned;

    public BookIssue(String issueId, String bookId, String readerId, Date issueDate, Date dueDate, Date returnDate, boolean isReturned) {
        this.issueId = issueId;
        this.bookId = bookId;
        this.readerId = readerId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.isReturned = isReturned;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getIssueId() { return issueId; }
    public String getBookId() { return bookId; }
    public String getReaderId() { return readerId; }
    public Date getIssueDate() { return issueDate; }
    public Date getDueDate() { return dueDate; }
    public Date getReturnDate() { return returnDate; }
    public boolean isReturned() { return isReturned; }
    public void setReturned(boolean returned) { this.isReturned = returned; }
    public void setReturnDate(Date returnDate) { this.returnDate = returnDate; }
}
