package com.tvs.vitalsign.repository.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.tvs.vitalsign.repository.database.dao.AnalysisDao;
import com.tvs.vitalsign.repository.database.entity.AnalysisEntity;

@Database(entities = {AnalysisEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public static final String databaseName = "app_db";
    private static AppDatabase mAppDatabase;

    public static AppDatabase getInstance(Context applicationContext) {
        if (mAppDatabase == null) {
            mAppDatabase = Room.databaseBuilder(applicationContext, AppDatabase.class,
                    databaseName).build();
        }
        return mAppDatabase;
    }

    public abstract AnalysisDao analysisDao();
}