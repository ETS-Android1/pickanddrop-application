package uk.ac.tees.aad.W9517102.Customer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import uk.ac.tees.aad.W9517102.Common.History;
import uk.ac.tees.aad.W9517102.Common.RecyclerviewAdapter;
import uk.ac.tees.aad.W9517102.R;
import uk.ac.tees.aad.W9517102.Sqlite.DbHistory;

public class RideHistoryActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<History>list;
    DbHistory dbHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history);
        list=new ArrayList<>();
        recyclerView=findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHistory=new DbHistory(this);
        list=dbHistory.getAlarmList();
        recyclerView.setAdapter(new RecyclerviewAdapter(list,this));

    }
}