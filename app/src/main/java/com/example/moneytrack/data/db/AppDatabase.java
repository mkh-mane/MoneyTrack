package com.example.moneytrack.data.db;

import com.example.moneytrack.data.user.UserEntity;
import com.example.moneytrack.data.user.UserDao;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {TransactionEntity.class, UserEntity.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract TransactionDao transactionDao();
    public abstract UserDao userDao();   // սա ավելացրու

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "moneytrack_db"
            ).build();
        }
        return INSTANCE;
    }
}
