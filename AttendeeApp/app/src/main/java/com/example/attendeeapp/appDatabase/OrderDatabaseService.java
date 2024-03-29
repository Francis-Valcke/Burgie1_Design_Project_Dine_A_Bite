package com.example.attendeeapp.appDatabase;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.room.Room;

import com.example.attendeeapp.json.CommonOrder;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * The local database service that is used to access and store orders in the local order database.
 */
public class OrderDatabaseService {

    private String DB_NAME = "order_db";

    private AppDatabase orderDatabase;

    public OrderDatabaseService(Context context) {
        orderDatabase = Room.databaseBuilder(context, AppDatabase.class, DB_NAME)
                .fallbackToDestructiveMigration()
                .build();
    }

    public void insertOrder(CommonOrder order) {
        new InsertOrderTask(orderDatabase, order).execute();
    }

    public void updateOrder(CommonOrder order) {
        new UpdateOrderTask(orderDatabase, order).execute();
    }

    public void deleteOrder(CommonOrder order) {
        new DeleteOrderTask(orderDatabase, order).execute();
    }

    public void deleteAllOrders() {
        new DeleteAllOrdersTask(orderDatabase).execute();
    }

    public List<CommonOrder> getAll() {
        try {
            return new GetOrdersTask(orderDatabase).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            Log.v("Get all exception", "Exception in get all orders");
        }
        return null;
    }

    /**
     * A task that is used to insert a given CommonOrder in the local order database.
     * It is executed on a different thread to prevent locking of the UI thread.
     */
    private static class InsertOrderTask extends AsyncTask<Void, Void, Void> {
        private AppDatabase orderDatabase;
        private CommonOrder order;

        InsertOrderTask(AppDatabase orderDatabase, CommonOrder order) {
            this.orderDatabase = orderDatabase;
            this.order = order;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            orderDatabase.orderDao().insertOrder(order);
            return null;
        }
    }

    /**
     * A task that is used to update a given CommonOrder in the local order database.
     * It is executed on a different thread to prevent locking of the UI thread.
     */
    private static class UpdateOrderTask extends AsyncTask<Void, Void, Void> {
        private AppDatabase orderDatabase;
        private CommonOrder order;

        UpdateOrderTask(AppDatabase orderDatabase, CommonOrder order) {
            this.orderDatabase = orderDatabase;
            this.order = order;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            orderDatabase.orderDao().updateOrder(order);
            return null;
        }
    }

    /**
     * A task that is used to delete a given orders from the local order database.
     * It is executed on a different thread to prevent locking of the UI thread.
     */
    private static class DeleteOrderTask extends AsyncTask<Void, Void, Void> {
        private AppDatabase orderDatabase;
        private CommonOrder order;

        DeleteOrderTask(AppDatabase orderDatabase, CommonOrder order) {
            this.orderDatabase = orderDatabase;
            this.order = order;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            orderDatabase.orderDao().deleteOrder(order);
            //orderDatabase.clearAllTables();
            return null;
        }
    }

    /**
     * A task that is used to delete all orders from the local order database.
     * It is executed on a different thread to prevent locking of the UI thread.
     */
    private static class DeleteAllOrdersTask extends AsyncTask<Void, Void, Void> {
        private AppDatabase orderDatabase;

        DeleteAllOrdersTask(AppDatabase orderDatabase) {
            this.orderDatabase = orderDatabase;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            orderDatabase.clearAllTables();
            return null;
        }
    }

    /**
     * A task that is used to get all orders from the local order database.
     * It is executed on a different thread to prevent locking of the UI thread.
     */
    private static class GetOrdersTask extends AsyncTask<Void, Void, List<CommonOrder>> {
        private AppDatabase orderDatabase;

        GetOrdersTask(AppDatabase orderDatabase) {
            this.orderDatabase = orderDatabase;
        }

        @Override
        protected List<CommonOrder> doInBackground(Void... voids) {
            return orderDatabase.orderDao().getAll();
        }
    }
}
