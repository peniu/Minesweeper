package com.example.mateusz.minesweeper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class Play_menu extends AppCompatActivity implements View.OnClickListener {

    private SharedPreferences configPref;
    private int height;
    private int width;
    private int bombs;
    // maximum height and width are determined by the reasonable size of a single
    // play field, while maximum amount of bombs is height times width
    private int MAX_HEIGHT = 9;
    private int MAX_WIDTH = 8;
    private int MAX_BOMBS;
    private TextView height_view;
    private TextView width_view;
    private TextView bombs_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_menu);
        // opening shared pref file used to store custom game settings
        configPref = getSharedPreferences("SweeperConfig", Context.MODE_PRIVATE);
        height = configPref.getInt("CUSTOM_HEIGHT", 5);
        width = configPref.getInt("CUSTOM_WIDTH", 5);
        bombs = configPref.getInt("CUSTOM_BOMBS", 7);
        MAX_BOMBS = height*width;
        // adding custom game settings to textviews
        height_view = (TextView)findViewById(R.id.height_view);
        height_view.setText(getString(R.string.board_height,height));
        width_view = (TextView)findViewById(R.id.width_view);
        width_view.setText(getString(R.string.board_width,width));
        bombs_view = (TextView)findViewById(R.id.bombs_view);
        bombs_view.setText(getString(R.string.bombs_number,bombs));
        // setting listeners for buttons
        ImageButton but1 = (ImageButton)findViewById(R.id.bombs_decrement);
        but1.setOnClickListener(this);
        ImageButton but2 = (ImageButton)findViewById(R.id.bombs_increment);
        but2.setOnClickListener(this);
        ImageButton but3 = (ImageButton)findViewById(R.id.width_decrement);
        but3.setOnClickListener(this);
        ImageButton but4 = (ImageButton)findViewById(R.id.width_increment);
        but4.setOnClickListener(this);
        ImageButton but5 = (ImageButton)findViewById(R.id.height_decrement);
        but5.setOnClickListener(this);
        ImageButton but6 = (ImageButton)findViewById(R.id.height_increment);
        but6.setOnClickListener(this);
    }

    public void openPlay_4x4(View view) {
        Intent intent = new Intent(this, Play.class);
        intent.putExtra("BOARD_HEIGHT",4);
        intent.putExtra("BOARD_WIDTH",4);
        intent.putExtra("BOARD_BOMBS",4);
        startActivity(intent);
    }

    public void openPlay_5x5(View view) {
        Intent intent = new Intent(this, Play.class);
        intent.putExtra("BOARD_HEIGHT",5);
        intent.putExtra("BOARD_WIDTH",5);
        intent.putExtra("BOARD_BOMBS",7);
        startActivity(intent);
    }

    public void openPlay_7x7(View view) {
        Intent intent = new Intent(this, Play.class);
        intent.putExtra("BOARD_HEIGHT",7);
        intent.putExtra("BOARD_WIDTH",7);
        intent.putExtra("BOARD_BOMBS",13);
        startActivity(intent);
    }

    public void openPlay(View view) {
        // opening play class and sending custom game settings
        Intent intent = new Intent(this, Play.class);
        intent.putExtra("BOARD_HEIGHT",height);
        intent.putExtra("BOARD_WIDTH",width);
        intent.putExtra("BOARD_BOMBS",bombs);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        // method changes properties of custom game, when buttons are used
        // there are limits on each parameter
        switch (v.getId() /*to get clicked view id**/) {
            case R.id.height_decrement:
                if(height>1){
                    height--;
                    height_view.setText(getString(R.string.board_height,height));
                }
                break;
            case R.id.height_increment:
                if(height<MAX_HEIGHT){
                    height++;
                    height_view.setText(getString(R.string.board_height,height));
                }
                break;
            case R.id.width_decrement:
                if(width>1){
                    width--;
                    width_view.setText(getString(R.string.board_width,width));
                }
                break;
            case R.id.width_increment:
                if(width<MAX_WIDTH){
                    width++;
                    width_view.setText(getString(R.string.board_width,width));
                }
                break;
            case R.id.bombs_decrement:
                if(bombs>1){
                    bombs--;
                    bombs_view.setText(getString(R.string.bombs_number,bombs));
                }
                break;
            case R.id.bombs_increment:
                if(bombs<MAX_BOMBS){
                    bombs++;
                    bombs_view.setText(getString(R.string.bombs_number,bombs));
                }
                break;
            default:
                break;
        }
        // updating maximum amount of bombs possible
        MAX_BOMBS = height*width;
        // lowering number of bombs to highest possible if user changed board size
        // in a way that set amount of bombs can not fit into it
        if(bombs>MAX_BOMBS){
            bombs = MAX_BOMBS;
            bombs_view.setText(getString(R.string.bombs_number,bombs));
        }
        // saving changed custom game settings
        SharedPreferences.Editor editor = configPref.edit();
        editor.putInt("CUSTOM_HEIGHT", height);
        editor.putInt("CUSTOM_WIDTH", width);
        editor.putInt("CUSTOM_BOMBS", bombs);
        editor.apply();
    }
}