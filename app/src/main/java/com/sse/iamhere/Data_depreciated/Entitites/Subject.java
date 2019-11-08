package com.sse.iamhere.Data_depreciated.Entitites;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.sse.iamhere.Utils.TimestampConverter;

import java.util.Date;

@Entity(tableName = "subject")
public class Subject {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id")
    private int id;

    @ColumnInfo(name="name")
    private String name;

    @ColumnInfo(name="datetime")
    @TypeConverters(TimestampConverter.class)
    private Date date;

    @ColumnInfo(name="date_str")
    private String date_str;

    public Subject(String name, String date_str) {
        this.name = name;
        this.date_str = date_str;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDate_str() {
        return date_str;
    }

    public void setDate_str(String date_str) {
        this.date_str = date_str;
    }
}
