package com.example.attendeeapp.appDatabase;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.attendeeapp.json.CommonOrder;

@Database(entities = {CommonOrder.class}, version = 7, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract OrderDao orderDao();
}
