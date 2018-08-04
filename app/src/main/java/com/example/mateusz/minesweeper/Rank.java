package com.example.mateusz.minesweeper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class Rank extends AppCompatActivity {

    private String[] nicks=new String[10];
    private int[] high_scores=new int[10];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);
        // getting information about which standard game mode rank we want to show (4x4,5x5,7x7)
        Intent intent = getIntent();
        String type = intent.getStringExtra("STANDARD_TYPE");
        // opening file with high scores, saving data to arrays
        loadScores(type);
        // creating layout
        createLayout(type);
    }

    private void createLayout(String type){
        // setting head to proper text
        TextView head=findViewById(R.id.rank_text);
        head.setText(getString(R.string.rank_head,type));
        // creating layout table with nicks and scores
        TableLayout table;
        table = findViewById(R.id.rank_table);
        for (int r = 0; r < 10; r++) {
            TableRow row = new TableRow(this);
            table.addView(row);
            TextView t1 = new TextView(this);
            t1.setText(getString(R.string.rank_nick,r+1,nicks[r]));
            TextView t2 = new TextView(this);
            int seconds = high_scores[r]/100;
            int decimals = (high_scores[r]%100)/10;
            int hundredths = (high_scores[r]%100)%10;
            t2.setText(getString(R.string.rank_time,seconds,decimals,hundredths));
            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            params.weight = 1.0f;
            params.gravity = Gravity.END;
            params.setMargins(0,0,0,30);
            t2.setLayoutParams(params);
            t2.setGravity(Gravity.END);
            row.addView(t1);
            row.addView(t2);
        }
    }

    private void loadScores(String type){
        SharedPreferences sharedPref = getSharedPreferences("SweeperHighScores", Context.MODE_PRIVATE);
        for(int i=0;i<10;i++){
            high_scores[i]= sharedPref.getInt(type+"_HIGHSCORE_"+i,999999);
            nicks[i]= sharedPref.getString(type+"_NICK_"+i,"Janek");
        }
    }

}