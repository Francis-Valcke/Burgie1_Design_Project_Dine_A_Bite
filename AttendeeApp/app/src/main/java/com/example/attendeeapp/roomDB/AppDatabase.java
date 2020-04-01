package com.example.attendeeapp.roomDB;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.attendeeapp.order.CommonOrder;

@Database(entities = {CommonOrder.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract OrderDao orderDao();
}
