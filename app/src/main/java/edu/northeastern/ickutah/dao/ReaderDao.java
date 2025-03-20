package edu.northeastern.ickutah.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import edu.northeastern.ickutah.models.Reader;

@Dao
public interface ReaderDao {
    @Insert
    void insert(Reader reader);

    @Query("SELECT * FROM readers ORDER BY name ASC")
    List<Reader> getAllReaders();

    @Query("SELECT * FROM readers WHERE readerId = :readerId LIMIT 1")
    Reader getReaderById(String readerId);

    @Query("SELECT * FROM readers WHERE phone = :phone LIMIT 1")
    Reader getReaderByPhone(String phone);

    @Update
    void update(Reader reader);

    @Delete
    void delete(Reader reader);

    // Search Queries
    @Query("SELECT * FROM readers WHERE name LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR readerId LIKE '%' || :query || '%'")
    List<Reader> searchReaders(String query);

    // Advanced
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<Reader> readers);
}