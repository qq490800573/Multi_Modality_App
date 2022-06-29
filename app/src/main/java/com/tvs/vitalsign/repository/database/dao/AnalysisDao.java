package com.tvs.vitalsign.repository.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tvs.vitalsign.repository.database.entity.AnalysisEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

@Dao
public interface AnalysisDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAnalysisEntity(AnalysisEntity analysisEntities);

    @Query("SELECT * FROM analysis ORDER BY utc_yyyy_MM_dd_date DESC LIMIT 1")
    Single<AnalysisEntity> getLatestAnalysis();

    @Query("SELECT * FROM analysis " +
            "WHERE utc_time >= :utcTimeFrom AND utc_time < :utcTimeTo " +
            "ORDER BY utc_yyyy_MM_dd_date ASC")
    Single<List<AnalysisEntity>> getAnalysis(Long utcTimeFrom, Long utcTimeTo);

    @Query("SELECT * FROM analysis WHERE utc_time = :utcTime")
    Single<AnalysisEntity> getAnalysis(Long utcTime);
}
