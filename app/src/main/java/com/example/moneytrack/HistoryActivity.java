package com.example.moneytrack;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneytrack.data.db.AppDatabase;
import com.example.moneytrack.data.db.TransactionDao;
import com.example.moneytrack.data.db.TransactionEntity;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private TransactionDao transactionDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.recyclerViewHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TransactionAdapter(new java.util.ArrayList<>());
        recyclerView.setAdapter(adapter);

        transactionDao = AppDatabase.getInstance(this).transactionDao();

        loadData();
    }

    private void loadData() {

        new Thread(() -> {

            List<TransactionEntity> list = transactionDao.getAllTransactions();

            runOnUiThread(() -> adapter.setData(list));

        }).start();
    }
}