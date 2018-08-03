package com.example.mateusz.minesweeper;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Main_menu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    public void openPlay_menu(View view) {
        Intent intent = new Intent(this, Play_menu.class);
        startActivity(intent);
    }

    public void openRank_menu(View view) {
        Intent intent = new Intent(this, Rank_menu.class);
        startActivity(intent);
    }

    public void openAbout(View view) {
        Intent intent = new Intent(this, About.class);
        startActivity(intent);
    }
}