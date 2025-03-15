package edu.northeastern.ickutah.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import edu.northeastern.ickutah.dao.BookDao;
import edu.northeastern.ickutah.dao.ReaderDao;
import edu.northeastern.ickutah.dao.BookIssueDao;
import edu.northeastern.ickutah.models.Book;
import edu.northeastern.ickutah.models.Reader;
import edu.northeastern.ickutah.models.BookIssue;

@Database(entities = {Book.class, Reader.class, BookIssue.class}, version = 1, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class LibraryDatabase extends RoomDatabase {

    private static LibraryDatabase instance;

    public abstract BookDao bookDao();
    public abstract ReaderDao readerDao();
    public abstract BookIssueDao bookIssueDao();

    public static synchronized LibraryDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            LibraryDatabase.class, "library_db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // TODO: Replace with background tasks later
                    .build();
        }
        return instance;
    }
}