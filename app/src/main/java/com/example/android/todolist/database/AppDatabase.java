package com.example.android.todolist.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

@Database(entities = {TaskEntry.class}, version = 1, exportSchema = false)
@TypeConverters(TypeConverter.class)
//epeidi einai abstract den xreiazetai na ylopoihsei tis methodous ths alla an thn kaneis
//extend ayth se mia allh klasi tote 8a prepei h allh na ylopoihsei kai twn 2 klasewn tis me8odous.
public abstract class AppDatabase extends RoomDatabase {

    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "todolist";
    private static AppDatabase sInstance;

    public static AppDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, AppDatabase.DATABASE_NAME)
                        //  .allowMainThreadQueries()
                        //Queris should be done in a separate thread to avoid locking in the UI
                        //we will allow this only temporary
                        .build();
            }
        }
        return sInstance;
    }

    //    Abstract methods means there is no default implementation for it and an implementing
    // class will provide the details.
    // You can also have as an return type an interface and handle the method from (it adds abstraction)
    //and its like you get the object that implements that interface.
    public abstract TaskDao taskDao();

}
