package com.example.mateusz.minesweeper;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Rank_menu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank_menu);
    }
    public void openRank_4x4(View view) {
        Intent intent = new Intent(this, Rank.class);
        intent.putExtra("STANDARD_TYPE","4x4");
        startActivity(intent);
    }
    public void openRank_5x5(View view) {
        Intent intent = new Intent(this, Rank.class);
        intent.putExtra("STANDARD_TYPE","5x5");
        startActivity(intent);
    }
    public void openRank_7x7(View view) {
        Intent intent = new Intent(this, Rank.class);
        intent.putExtra("STANDARD_TYPE","7x7");
        startActivity(intent);
    }
}