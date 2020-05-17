package com.example.attendeeapp.appDatabase;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.attendeeapp.json.CommonOrder;

/**
 * The device database for storing local data.
 */
@Database(entities = {CommonOrder.class}, version = 7, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    /**
     * Method to request the order database access object
     * to get access to orders from the local database.
     *
     * @return The currently used order database access object.
     */
    public abstract OrderDao orderDao();
}
