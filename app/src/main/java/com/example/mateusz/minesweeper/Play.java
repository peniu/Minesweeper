package com.example.mateusz.minesweeper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Play extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    SharedPreferences sharedPref;
    private int BOARD_WIDTH;
    private int BOARD_HEIGHT;
    private int BOARD_BOMBS;
    private int fields_number;
    private int result=0;
    private int NR,x,y;
    private TextView text_timer;
    private Timer T;
    private int[] field_labels;
    private int[] guesses;
    private int[] toUncover;
    private int[] toCheckifzero;
    private int[] checked;
    private boolean standard=false;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        // loading file used to store high scores
        sharedPref = getSharedPreferences("SweeperHighScores", Context.MODE_PRIVATE);
        // grabbing settings of custom game from intent
        Intent intent = getIntent();
        BOARD_HEIGHT = intent.getIntExtra("BOARD_HEIGHT",5);
        BOARD_WIDTH = intent.getIntExtra("BOARD_WIDTH",5);
        BOARD_BOMBS = intent.getIntExtra("BOARD_BOMBS",7);
        // checking if its one of "standard" game modes (4x4,5x5,7x7)
        if(BOARD_HEIGHT == 4 && BOARD_WIDTH == 4 && BOARD_BOMBS == 4){
            standard = true;
            type = "4x4";
        }
        if(BOARD_HEIGHT == 5 && BOARD_WIDTH == 5 && BOARD_BOMBS == 7){
            standard = true;
            type = "5x5";
        }
        if(BOARD_HEIGHT == 7 && BOARD_WIDTH == 7 && BOARD_BOMBS == 13){
            standard = true;
            type = "7x7";
        }
        // method which creates layout with given parameters
        createLayout(BOARD_HEIGHT,BOARD_WIDTH);
        fields_number = BOARD_HEIGHT*BOARD_WIDTH;
        // initialization of arrays used for the mechanics of the game
        toUncover = new int[fields_number];
        toCheckifzero = new int[fields_number];
        guesses = new int[fields_number];
        checked = new int[fields_number];
        field_labels = new int[fields_number];
        for(int i=0;i<fields_number;i++){
            toUncover[i] = 0;
            toCheckifzero[i] = 0;
            guesses[i] = 0;
            checked[i] = 0;
            field_labels[i] = 0;
        }
        prepareBoard();
        // timer and updating textview showing time over game field
        result=0;
        T = new Timer();
        text_timer = (TextView)findViewById(R.id.ttimer);
        T.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int seconds = result/100;
                        int decimals = (result%100)/10;
                        int hundredths = (result%100)%10;
                        text_timer.setText("Time= "+seconds+"."+decimals+hundredths+"s");
                    }
                });
                result++;
            }
        }, 500, 10);
    }

    @Override
    public void onClick (View v) {
        int click_ID = v.getId();
        int exit = 0;
        int flag = 1;
        if(click_ID==R.id.res_button){
            exit = 1;
            T.cancel();
            recreate();
        }
        //sprawdza czy zero, jesli tak to sprawdza czy okoliczne to zera
        //itd do momentu gdy nie nastapi zadna zmiana w tablicy toCheckifzero
        //po wywolaniu Checkifzero, co oznacza
        //ze kazde okoliczne pole to pole z numerkiem niezerowym
        if(exit!=1){
            Checkifzero(click_ID);
            while(flag!=0){
                flag=0;
                for(int i=0;i<fields_number;i++){
                    if(toCheckifzero[i]==1){
                        Checkifzero(i);
                        toCheckifzero[i]=0;
                        flag=1;
                    }
                }
            }
            //odslania wszystkie pola ktore zostaly oznaczone jako do odkrycia
            //czyli numerki, oraz w przypadku trafienia w zero - okoliczne zera
            //i okoliczne numerki
            for(int i=0;i<fields_number;i++){
                if(toUncover[i]==1){
                    Uncover(i);
                }
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        int click_ID = v.getId();
        MarkBomb(click_ID);
        return true;
    }

    // methods which allow to change from number of field
    // to its coordinates and vice versa
    private int getXfromNR(int NR) {
        return NR % BOARD_WIDTH;
    }

    private int getYfromNR(int NR) {
        return NR / BOARD_WIDTH;
    }

    private int getNRfromXY(int X, int Y) {
        return Y*BOARD_WIDTH+X;
    }

    private void prepareBoard() {
        // method prepares the board - randomly selects bomb localizations
        // and describes each field on the board with number of bombs nearby
        int[] bomb_index = new int[BOARD_BOMBS];
        for(int i=0;i<BOARD_BOMBS;i++){
            bomb_index[i]=BOARD_HEIGHT*BOARD_WIDTH;
        }
        int bombs_assigned = 0;
        int flag;
        while (bombs_assigned < BOARD_BOMBS) {
            flag = 0;
            Random r = new Random();
            int g = r.nextInt(fields_number);
            for (int i = 0; i < BOARD_BOMBS; i++) {
                // to check if there is a bomb already
                if (g == bomb_index[i]) {
                    flag = 1;
                    break;
                }
            }
            // if there is no bomb -> adding field number to bomb_index
            if (flag == 0) {
                bomb_index[bombs_assigned] = g;
                bombs_assigned++;
            }
        }
        // changing indexes of bombs into x and y coordinates
        int[] x_bomb = new int[BOARD_BOMBS];
        int[] y_bomb = new int[BOARD_BOMBS];
        for (int i = 0; i < BOARD_BOMBS; i++) {
            x_bomb[i] = getXfromNR(bomb_index[i]);
            y_bomb[i] = getYfromNR(bomb_index[i]);
        }
        // adding labels for each field of the board
        // 0-8 number of bombs nearby
        // 9 bomb
        for(int j=0;j<BOARD_BOMBS;j++) {
            if (x_bomb[j] - 1 >= 0 && y_bomb[j] - 1 >= 0) {
                field_labels[getNRfromXY(x_bomb[j] - 1, y_bomb[j] - 1)]++;
            }
            if (x_bomb[j] - 1 >= 0) {
                field_labels[getNRfromXY(x_bomb[j] - 1, y_bomb[j])]++;
            }
            if (x_bomb[j] - 1 >= 0 && y_bomb[j] + 1 <= BOARD_HEIGHT-1) {
                field_labels[getNRfromXY(x_bomb[j] - 1, y_bomb[j]+1)]++;
            }
            if (y_bomb[j] - 1 >= 0) {
                field_labels[getNRfromXY(x_bomb[j], y_bomb[j]-1)]++;
            }
            if (y_bomb[j] + 1 <= BOARD_HEIGHT-1) {
                field_labels[getNRfromXY(x_bomb[j], y_bomb[j]+1)]++;
            }
            if (x_bomb[j] + 1 <= BOARD_WIDTH-1 && y_bomb[j] - 1 >= 0) {
                field_labels[getNRfromXY(x_bomb[j] +1, y_bomb[j]-1)]++;
            }
            if (x_bomb[j] + 1 <= BOARD_WIDTH-1) {
                field_labels[getNRfromXY(x_bomb[j] + 1, y_bomb[j])]++;
            }
            if (x_bomb[j] + 1 <= BOARD_WIDTH-1 && y_bomb[j] + 1 <= BOARD_HEIGHT-1) {
                field_labels[getNRfromXY(x_bomb[j] + 1, y_bomb[j]+1)]++;
            }
        }
        for(int j=0;j<BOARD_BOMBS;j++){
            field_labels[bomb_index[j]] = 9;
        }
    }

    private void createLayout(int HEIGHT,int WIDTH){
        // Creating table layout
        TableLayout table;
        table = (TableLayout) findViewById(R.id.view_root);

        // Adding buttons to the board
        int i=0;
        for (int y = 0; y < HEIGHT; y++) {
            TableRow row = new TableRow(this);
            table.addView(row);
            for (int x = 0; x < WIDTH; x++) {
                Button b = new Button(this);
                b.setId(i);
                b.setText("");
                b.setOnClickListener(this);
                b.setOnLongClickListener(this);
                i++;
                row.addView(b);
            }
        }
        Button restart_button = (Button)findViewById(R.id.res_button);
        restart_button.setOnClickListener(this);
    }

    private void Checkifzero(int nr){
        //metoda sprawdza czy trafilismy w zero, a jesli tak to dodaje do tablicy toUncover
        //1 w indeksach ktore trzeba otworzyc
        //jesli w tych okolicach jest zero to trzeba znowu wywolac Checkifzero
        checked[nr]=1; //zapobiega aby dwa zera sprawdzaly sie nawzajem w nieskonczonosc
        toUncover[nr]=1; //bo pole sprawdzane zawsze trzeba otworzyc (uncover)
        if (field_labels[nr] == 0){
            x = getXfromNR(nr);
            y = getYfromNR(nr);
            if (x - 1 >= 0 && y - 1 >= 0) {
                NR = getNRfromXY(x - 1, y - 1);
                toUncover[NR] = 1;
                if(field_labels[NR]==0 && checked[NR]==0){
                    toCheckifzero[NR]=1;
                }
            }
            if (x - 1 >= 0) {
                NR = getNRfromXY(x - 1, y);
                toUncover[NR] = 1;
                if(field_labels[NR]==0 && checked[NR]==0){
                    toCheckifzero[NR]=1;
                }
            }
            if (x - 1 >= 0 && y + 1 <= BOARD_HEIGHT-1) {
                NR = getNRfromXY(x - 1, y + 1);
                toUncover[NR] = 1;
                if(field_labels[NR]==0 && checked[NR]==0){
                    toCheckifzero[NR]=1;
                }
            }
            if (y - 1 >= 0) {
                NR = getNRfromXY(x, y - 1);
                toUncover[NR] = 1;
                if(field_labels[NR]==0 && checked[NR]==0){
                    toCheckifzero[NR]=1;
                }
            }
            if (y + 1 <= BOARD_HEIGHT-1) {
                NR = getNRfromXY(x, y + 1);
                toUncover[NR] = 1;
                if(field_labels[NR]==0 && checked[NR]==0){
                    toCheckifzero[NR]=1;
                }
            }
            if (x + 1 <= BOARD_WIDTH-1 && y - 1 >= 0) {
                NR = getNRfromXY(x + 1, y - 1);
                toUncover[NR] = 1;
                if(field_labels[NR]==0 && checked[NR]==0){
                    toCheckifzero[NR]=1;
                }
            }
            if (x + 1 <= BOARD_WIDTH-1) {
                NR = getNRfromXY(x + 1, y);
                toUncover[NR] = 1;
                if(field_labels[NR]==0 && checked[NR]==0){
                    toCheckifzero[NR]=1;
                }
            }
            if (x + 1 <= BOARD_WIDTH-1 && y + 1 <= BOARD_HEIGHT-1) {
                NR = getNRfromXY(x + 1, y + 1);
                toUncover[NR] = 1;
                if(field_labels[NR]==0 && checked[NR]==0){
                    toCheckifzero[NR]=1;
                }
            }
        }
    }

    //RECREATE TO EDIT UNCOVER
    private void Uncover (int nr) {
        Button b = (Button)findViewById(nr);
        if (field_labels[nr] == 9) {
            //czyli na tym przycisku byla bomba -> game over, od nowa lecimy
            b.setText("*");
            T.cancel();
            //recreate();
        }
        else {
            //tzn ze pole z numerkiem -> wyswietlamy numerek
            b.setText(String.valueOf(field_labels[nr]));
            guesses[nr]=1;
        }
        //jesli wszystkie pola odkryte i bomby prawidlowo oznaczone -> wywolanie win()
        if(CheckWin()){
            win();
        }
    }

    private void MarkBomb(int nr) {
        //jesli odkryte -> sprawdz okoliczne
        //liczymy bomby dookola przycisku, jesli jest oznaczonych tyle ile powinno
        //wg field_labels to albo otwieramy pozostale pola (jesli byly dobrze oznaczone wszystkie)
        //albo wybuchamy (jesli chociaz 1 bomba byla zle oznaczona lub za duzo bomb oznaczono)
        //jesli nie ma oznaczonych wystarczajaco bomb -> nic nie robimy
        Button b = (Button)findViewById(nr);
        if(guesses[nr]==1){
            int[] teOdkryj = new int[8];
            for(int i=0;i<8;i++){
                teOdkryj[i]=50;
            }
            int bomby_ok = 0;
            int bomby_bad = 0;
            x = getXfromNR(nr);
            y = getYfromNR(nr);
            int g=0;
            NR=getNRfromXY(x-1,y-1);
            if (x - 1 >= 0 && y - 1 >= 0) {
                //jesli jest poprawnie lub zle oznaczona bomba -> policz
                if(guesses[NR]==2){
                    bomby_bad++;
                    teOdkryj[g]=NR;
                }
                else if(guesses[NR]==999){
                    bomby_ok++;
                }
                else{
                    teOdkryj[g]=NR;
                }
                g++;
            }
            NR=getNRfromXY(x-1,y);
            if (x - 1 >= 0) {
                if(guesses[NR]==2){
                    bomby_bad++;
                    teOdkryj[g]=NR;
                }
                else if(guesses[NR]==999){
                    bomby_ok++;
                }
                else{
                    teOdkryj[g]=NR;
                }
                g++;
            }
            NR=getNRfromXY(x-1,y+1);
            if (x - 1 >= 0 && y + 1 <= BOARD_HEIGHT-1) {
                if(guesses[NR]==2){
                    bomby_bad++;
                    teOdkryj[g]=NR;
                }
                else if(guesses[NR]==999){
                    bomby_ok++;
                }
                else{
                    teOdkryj[g]=NR;
                }
                g++;
            }
            NR=getNRfromXY(x,y-1);
            if (y - 1 >= 0) {
                if(guesses[NR]==2){
                    bomby_bad++;
                    teOdkryj[g]=NR;
                }
                else if(guesses[NR]==999){
                    bomby_ok++;
                }
                else{
                    teOdkryj[g]=NR;
                }
                g++;
            }
            NR=getNRfromXY(x,y+1);
            if (y + 1 <= BOARD_HEIGHT-1) {
                if(guesses[NR]==2){
                    bomby_bad++;
                    teOdkryj[g]=NR;
                }
                else if(guesses[NR]==999){
                    bomby_ok++;
                }
                else{
                    teOdkryj[g]=NR;
                }
                g++;
            }
            NR=getNRfromXY(x+1,y-1);
            if (x + 1 <= BOARD_WIDTH-1 && y - 1 >= 0) {
                if(guesses[NR]==2){
                    bomby_bad++;
                    teOdkryj[g]=NR;
                }
                else if(guesses[NR]==999){
                    bomby_ok++;
                }
                else{
                    teOdkryj[g]=NR;
                }
                g++;
            }
            NR=getNRfromXY(x+1,y);
            if (x + 1 <= BOARD_WIDTH-1) {
                if(guesses[NR]==2){
                    bomby_bad++;
                    teOdkryj[g]=NR;
                }
                else if(guesses[NR]==999){
                    bomby_ok++;
                }
                else{
                    teOdkryj[g]=NR;
                }
                g++;
            }
            NR=getNRfromXY(x+1,y+1);
            if (x + 1 <= BOARD_WIDTH-1 && y + 1 <= BOARD_HEIGHT-1) {
                if(guesses[NR]==2){
                    bomby_bad++;
                    teOdkryj[g]=NR;
                }
                else if(guesses[NR]==999){
                    bomby_ok++;
                }
                else{
                    teOdkryj[g]=NR;
                }
            }
            if(bomby_ok+bomby_bad>=field_labels[nr]){
                for(int i=0;i<8;i++){
                    if(teOdkryj[i]!=50){
                        Uncover(teOdkryj[i]);
                    }
                }
            }
        }

        //jesli zaznaczone jako bomba -> oznacz jako puste
        else if (guesses[nr] == 999 || guesses[nr]==2) {
            b.setText(" ");
            guesses[nr] = 0;
        }
        //nie zaznaczone i jest bomba -> prawidlowo
        else if(field_labels[nr]==9){
            b.setText("X");
            guesses[nr] = 999;
        }
        //nie zanzaczone i nie ma bomby -> nieprawidlowo
        else{
            b.setText("X");
            guesses[nr] = 2;
        }
        //sprawdzenie czy po oznaczeniu pola nastapila wygrana
        if(CheckWin()){
            win();
        }
    }

    private void win(){
        // method checks if its one of standard game modes
        // if not, it shows congratulations and time
        // if yes, and result is TOP 10, asks for nick and saves score
        final int score = result;
        T.cancel();
        int seconds = score/100;
        int decimals = (score%100)/10;
        int hundredths = (score%100)%10;
        text_timer=(TextView)findViewById(R.id.ttimer);
        String time = seconds+"."+decimals+hundredths+"s";
        text_timer.setText("Time= "+time);

        //dialog pytajacy o nick przy wygranej i dostaniu sie do TOP 10 w standardowej grze
        final int Rank = CheckRank(score);
        if(Rank<10 && standard){
            AlertDialog.Builder alert1 = new AlertDialog.Builder(this);
            final EditText edittext = new EditText(this);
            alert1.setTitle("You made it to TOP10 in: "+time);
            alert1.setMessage("Provide your nickname");
            alert1.setView(edittext);
            alert1.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String nick = edittext.getText().toString();
                    MoveScores(Rank,score,nick);
                    finish();
                }
            });
            alert1.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    finish();
                }
            });
            alert1.show();
        }
        //dialog informujacy o czasie, bez dostania do TOP10
        else{
            AlertDialog.Builder alert2 = new AlertDialog.Builder(this);
            alert2.setTitle("You did it in: "+time);
            alert2.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    finish();
                }
            });
            alert2.show();

        }
    }

    private boolean CheckWin(){
        int win_flag=0;
        for (int i = 0; i < fields_number; i++) {
            if ((guesses[i] == 999 && field_labels[i] == 9) || (guesses[i] == 1 && field_labels[i] != 9)) {

            }
            else {
                win_flag++;
            }
        }
        if (win_flag == 0) {
            return true;
        }
        else{
            return false;
        }
    }

    private int CheckRank(int score){
        //pobiera score i sprawdza czy oraz ktory na TOP10 jest
        if(standard){
            int high_scores[]=new int[10];
            for(int i=0;i<10;i++){
                high_scores[i]= sharedPref.getInt(type+"_HIGHSCORE_"+i,999999);
                if(score<=high_scores[i]){
                    return i;
                }
            }
        }
        return 10;
    }

    private void MoveScores(int rank, int new_score, String new_nick){
        //zapisuje score na odpowiednim miejscu i przesuwa wyniki gorsze o 1 pozycje
        int high_scores[]=new int[10];
        String nicks[]=new String[10];
        for(int i=0;i<10;i++){
            high_scores[i]= sharedPref.getInt(type+"_HIGHSCORE_"+i,999999);
            nicks[i]= sharedPref.getString(type+"_NICK_"+i,"Janek");
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        //zapisanie nowego wyniku na odpowiednim miejscu
        editor.putInt(type+"_HIGHSCORE_"+rank, new_score);
        editor.putString(type+"_NICK_"+rank,new_nick);
        //zapisanie reszty wynikow przestunietych o 1 miejsce dalej
        for(int i=rank+1;i<10;i++){
            editor.putInt(type+"_HIGHSCORE_"+i, high_scores[i-1]);
            editor.putString(type+"_NICK_"+i,nicks[i-1]);
        }
        editor.apply();
    }

}