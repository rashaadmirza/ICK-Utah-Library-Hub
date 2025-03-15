package edu.northeastern.ickutah.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "readers")
public class Reader implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String readerId;
    private String name;
    private String email;
    private String phone;
    private int currentCheckouts;

    public Reader(String readerId, String name, String email, String phone, int currentCheckouts) {
        this.readerId = readerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.currentCheckouts = currentCheckouts;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getReaderId() { return readerId; }
    public void setReaderId(String readerId) { this.readerId = readerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public int getCurrentCheckouts() { return currentCheckouts; }
    public void setCurrentCheckouts(int currentCheckouts) { this.currentCheckouts = currentCheckouts; }
}