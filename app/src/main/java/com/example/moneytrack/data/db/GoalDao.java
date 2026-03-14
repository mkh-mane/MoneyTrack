package com.example.moneytrack.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface GoalDao {

    @Insert
    void insert(GoalEntity goal);

    @Query("SELECT * FROM goals")
    List<GoalEntity> getAllGoals();
}
