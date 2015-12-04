package com.example.eirik.fightgame;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // Log tag
    private static final String TAG = "LogMainActivity";

    // SharedPreference key for saved game
    public static final String SAVE = "MySavedGame";
    public static final String MUSIC = "ToggleMusic";

    int playerOne = 100;
    int playerTwo = 100;

    int round = 1;

    EditText editTextPlayerOne;
    EditText editTextPlayerTwo;

    ProgressBar progressOne;
    ProgressBar progressTwo;

    Button playerOnePunch;
    Button playerOneBlock;
    Button playerTwoPunch;
    Button playerTwoBlock;
    Button buttonBottom;

    ImageView imagePlayerOne;
    ImageView imagePlayerTwo;

    TextView bottomText;

    boolean playerOneBlocking = false;
    boolean playerTwoBlocking = false;

    MediaPlayer myMusic;
    boolean musicBoolean = true;

    SoundPool mySound;
    int spApplause;
    int spJab;
    int spBlock;

    Handler handler;
    Runnable runnableOne;
    Runnable runnableTwo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextPlayerOne = (EditText) findViewById(R.id.editTextPlayer1);
        editTextPlayerTwo = (EditText) findViewById(R.id.editTextPlayer2);

        Bundle bundle = getIntent().getExtras();
        try {
            if (bundle.getBoolean("extraBool", false)) {
                round = bundle.getInt("extraRound", 11);
                editTextPlayerOne.setText(bundle.getString("nameOne", "PlayerOne"));
                editTextPlayerTwo.setText(bundle.getString("nameTwo", "PlayerTwo"));
            }
        } catch (Exception e) {
            Log.d(TAG, "If null pointer it just mean it's a new game. Error: " + e.toString());
        }

        progressOne = (ProgressBar) findViewById(R.id.progressBarPlayer1);
        progressTwo = (ProgressBar) findViewById(R.id.progressBarPlayer2);

        progressOne.setProgress(playerOne);
        progressTwo.setProgress(playerTwo);

        playerOnePunch = (Button) findViewById(R.id.buttonPunch1);
        playerOneBlock = (Button) findViewById(R.id.buttonBlock1);
        playerTwoPunch = (Button) findViewById(R.id.buttonPunch2);
        playerTwoBlock = (Button) findViewById(R.id.buttonBlock2);

        imagePlayerOne = (ImageView) findViewById(R.id.imageViewPlayer1);
        imagePlayerTwo = (ImageView) findViewById(R.id.imageViewPlayer2);

        bottomText = (TextView) findViewById(R.id.textViewBottom);
        buttonBottom = (Button) findViewById(R.id.buttonBottom);

        bottomText.setText(String.format(getResources().getString(R.string.round), String.valueOf(round)));

        mySound = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
        spJab = mySound.load(this, R.raw.jab, 1);
        spApplause = mySound.load(this, R.raw.applause, 1);
        spBlock = mySound.load(this, R.raw.block, 1);

        handler = new Handler();

        runnableOne = new Runnable() {
            @Override
            public void run() {
                imagePlayerOne.setImageResource(R.drawable.player1);
                //handler.postDelayed(this, 100);
            }
        };

        runnableTwo = new Runnable() {
            @Override
            public void run() {
                imagePlayerTwo.setImageResource(R.drawable.player2);
                //handler.postDelayed(this, 100);
            }
        };

        loadMusicSettings();

        // App bar, using support v7 library
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // App icon
        try {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } catch (Exception e) {
            Log.d(TAG, "Something wrong with SupportActionBar " + e.toString());
        }
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        // Listener for hold block buttons
        playerOneBlock.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    handler.removeCallbacks(runnableOne);
                    playerOneBlocking = true;
                    imagePlayerOne.setImageResource(R.drawable.player1_block);
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    playerOneBlocking = false;
                    imagePlayerOne.setImageResource(R.drawable.player1);
                }

                return false;
            }

        });

        playerTwoBlock.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    handler.removeCallbacks(runnableTwo);
                    playerTwoBlocking = true;
                    imagePlayerTwo.setImageResource(R.drawable.player2_block);
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    playerTwoBlocking = false;
                    imagePlayerTwo.setImageResource(R.drawable.player2);
                }

                return false;
            }

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.app_bar_layout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_load:

                SharedPreferences loadGame = getSharedPreferences(SAVE, MODE_PRIVATE);
                round = loadGame.getInt("savedRound", 12);
                playerOne = loadGame.getInt("savedPlayer1Health", 50);
                playerTwo = loadGame.getInt("savedPlayer2Health", 50);
                progressOne.setProgress(playerOne);
                progressTwo.setProgress(playerTwo);

                editTextPlayerOne.setText(loadGame.getString("namePlayerOne", "Default 1"));
                editTextPlayerTwo.setText(loadGame.getString("namePlayerTwo", "Default 2"));
                bottomText.setText(String.format(getResources().getString(R.string.round), String.valueOf(round)));

                checkHealthPlayerOne();
                checkHealthPlayerTwo();

                Toast.makeText(MainActivity.this, "Game loaded", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.menu_save:

                editTextPlayerOne = (EditText) findViewById(R.id.editTextPlayer1);
                editTextPlayerTwo = (EditText) findViewById(R.id.editTextPlayer2);

                String nameOne = editTextPlayerOne.getText().toString();
                String nameTwo = editTextPlayerTwo.getText().toString();
                Log.d(TAG, "Name Player1: " + nameOne);
                Log.d(TAG, "Name Player2: " + nameTwo);

                SharedPreferences saveGame = getSharedPreferences(SAVE, MODE_PRIVATE);
                SharedPreferences.Editor editor = saveGame.edit();
                editor.putInt("savedPlayer1Health", playerOne).putInt("savedPlayer2Health", playerTwo).putInt("savedRound", round);
                editor.putString("namePlayerOne", nameOne).putString("namePlayerTwo", nameTwo);
                editor.apply();
                Toast.makeText(MainActivity.this, "Game saved", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.menu_newgame:
                resetGame(false);


            case R.id.menu_about:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.menu_about)
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage(R.string.menu_about_message)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing. This should remove the box.
                            }
                        })
                        .show();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }


    public void playerOnePunch(View view) {

        if (playerTwoBlocking) {
            mySound.play(spBlock, 0.8f, 1, 1, 0, 1);
            imagePlayerOne.setImageResource(R.drawable.player1_jab);
            handler.postDelayed(runnableOne, 200);
            return;
        }

        if (!playerOneBlocking) {
            mySound.play(spJab, 1, 0.8f, 1, 0, 1);
            playerTwo = playerTwo - 5;
            progressTwo.setProgress(playerTwo);
            checkHealthPlayerTwo();
        }
    }

    public void PlayerTwoPunch(View view) {

        if (playerOneBlocking) {
            mySound.play(spBlock, 0.8f, 1, 1, 0, 1);
            imagePlayerTwo.setImageResource(R.drawable.player2_jab);
            handler.postDelayed(runnableTwo, 200);
            return;
        }

        if (!playerTwoBlocking) {
            mySound.play(spJab, 0.8f, 1, 1, 0, 1);
            playerOne = playerOne - 5;
            progressOne.setProgress(playerOne);
            checkHealthPlayerOne();
        }
    }

    private void checkHealthPlayerOne() {

        if (playerOne > 0) {
            imagePlayerTwo.setImageResource(R.drawable.player2_jab);
            handler.postDelayed(runnableTwo, 200);
        } else {
            handler.removeCallbacks(runnableOne);
            handler.removeCallbacks(runnableTwo);
            imagePlayerOne.setImageResource(R.drawable.player1_beaten);
            imagePlayerTwo.setImageResource(R.drawable.player2_winner);

            mySound.play(spApplause, 1, 1, 1, 0, 1);

            String tempString = editTextPlayerTwo.getText().toString();
            String tempValue = getResources().getString(R.string.bottom_player_wins);
            bottomText.setText(String.format(tempValue, tempString));

            playerOnePunch.setEnabled(false);
            playerTwoPunch.setEnabled(false);
            playerOneBlock.setEnabled(false);
            playerTwoBlock.setEnabled(false);

            buttonBottom.setVisibility(View.VISIBLE);
            buttonBottom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetGame(true);
                }
            });
        }
    }

    private void checkHealthPlayerTwo() {

        if (playerTwo > 0) {
            imagePlayerOne.setImageResource(R.drawable.player1_jab);
            handler.postDelayed(runnableOne, 200);
        } else {
            handler.removeCallbacks(runnableOne);
            handler.removeCallbacks(runnableTwo);
            imagePlayerOne.setImageResource(R.drawable.player1_winner);
            imagePlayerTwo.setImageResource(R.drawable.player2_beaten);

            mySound.play(spApplause, 1, 1, 1, 0, 1);

            String tempString = editTextPlayerOne.getText().toString();
            String tempValue = getResources().getString(R.string.bottom_player_wins);
            bottomText.setText(String.format(tempValue, tempString));

            playerOnePunch.setEnabled(false);
            playerTwoPunch.setEnabled(false);
            playerOneBlock.setEnabled(false);
            playerTwoBlock.setEnabled(false);

            buttonBottom.setVisibility(View.VISIBLE);
            buttonBottom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetGame(true);
                }
            });

        }
    }

    private void resetGame(boolean b) {

        //recreate();

        Intent intent = getIntent();
        // True = new round and names remain the same, this happens when one wins and game restarts.
        // If New Game is pressed in the menu, the game will reset all values
        if (b) {
            round = round + 1;
            intent.putExtra("extraBool", true);
            intent.putExtra("extraRound", round);
            intent.putExtra("nameOne", editTextPlayerOne.getText().toString());
            intent.putExtra("nameTwo", editTextPlayerTwo.getText().toString());
        } else {
            intent.putExtra("extraBool", false);
        }
        finish();
        startActivity(intent);

        /*
        playerOne = 100;
        playerTwo = 100;
        progressOne.setProgress(playerOne);
        progressTwo.setProgress(playerTwo);
        imagePlayerOne.setImageResource(R.drawable.player1);
        imagePlayerTwo.setImageResource(R.drawable.player2);
        bottomText.setText("");
        buttonBottom.setVisibility(View.GONE);

        editTextPlayerOne.setText(R.string.player1);
        editTextPlayerTwo.setText(R.string.player2);

        playerOnePunch.setEnabled(true);
        playerTwoPunch.setEnabled(true);
        playerOneBlock.setEnabled(true);
        playerTwoBlock.setEnabled(true);
        */
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (myMusic != null) {
            myMusic.pause();
        }

        // bytt til onStop() ? http://developer.android.com/intl/pt-br/guide/topics/media/mediaplayer.html


        //super.onPause();
        //if (myMusic != null) {
        //    myMusic.release();
        //    myMusic = null;
        //}
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (myMusic != null) {
            myMusic.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (myMusic != null) {
            myMusic.release();
            myMusic = null;
        }
    }

    public void toggleMusic() {

        // musicBoolean true = turn on  false = turn off

        if (musicBoolean) {
            if (myMusic == null) {

                Thread musicThread = new Thread() {
                    @Override
                    public void run() {

                        myMusic = MediaPlayer.create(MainActivity.this, R.raw.black_rock);
                        myMusic.setLooping(true);
                        myMusic.start();
                        /*
                        myMusic.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                mp.start();
                            }
                        });
                        */

                    }
                };
                musicThread.start();

            }
        } else {
            if (myMusic != null) {
                myMusic.release();
                myMusic = null;
            }
        }

            /*
            if (myMusic == null) {
                myMusic = MediaPlayer.create(this, R.raw.black_rock);
                myMusic.setLooping(true);
                myMusic.start();
            } else {
                myMusic.release();
                myMusic = null;
            }
            */
    }

    public void loadMusicSettings() {

        // musicBoolean true = on false = off
        try {
            SharedPreferences loadMusic = getSharedPreferences(MUSIC, MODE_PRIVATE);
            musicBoolean = loadMusic.getBoolean("toggle", true);
        } catch (Exception e) {
            Log.d(TAG, "Error loading music settings. No worries, just not set yet. " + e.toString());
        }
        toggleMusic();
    }

    public void saveMusicSettings(MenuItem item) {

        musicBoolean = !musicBoolean;

        SharedPreferences toggleMusic = getSharedPreferences(MUSIC, MODE_PRIVATE);
        SharedPreferences.Editor musicEditor = toggleMusic.edit();
        musicEditor.putBoolean("toggle", musicBoolean);
        musicEditor.apply();

        toggleMusic();
    }
}
