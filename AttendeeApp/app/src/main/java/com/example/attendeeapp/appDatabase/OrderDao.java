package com.example.attendeeapp.appDatabase;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.attendeeapp.json.CommonOrder;

import java.util.List;

@Dao
public interface OrderDao {
    @Query("SELECT * FROM CommonOrder")
    List<CommonOrder> getAll();

    @Insert
    void insertOrder(CommonOrder order);

    @Update
    void updateOrder(CommonOrder order);

    @Delete
    void deleteOrder(CommonOrder order);


}
