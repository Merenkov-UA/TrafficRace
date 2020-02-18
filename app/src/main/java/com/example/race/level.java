package com.example.race;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import com.example.race.R;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.TimerTask;

public class level extends AppCompatActivity {

    private Animation alphaTransition;
    private Animation scaleTransition;

    private float roadTranslation;
    private float carTranslation;
    private float fuelTranslationY;
    private float fuelTranslationX;
    private float spikeTranslationY;
    private float spikeTranslationX;
    private float coinTranslationY;
    private float coinTranslationX;
    private float roadSpeed;

    private float outOfScreenTranslation = 500f;
    private float fuelAppearTranslation = -200f;
    private float coinAppearTranslation = -200f;
    private float spikeAppearTranslation = -200f;
    private float fuelTankTimeLeft;

    private ImageView roadImg;
    private ImageView carImg;
    private ImageView spikeImg;
    private ImageView coinImg;
    private ImageView fuelImage;

    private int interval;
    private int fuelCount;
    private int fuelTankInterval;
    private int cnt;
    private int scoreTv;
    private int life, maxScore, saveScore;

    private final String maxScoreFileName = "maxScore.dat";

    private Random rnd;


    private Runnable fuel;
    private Runnable speed;
    private Runnable score;
    private Runnable Health;
    private Runnable timerTick;


    private Handler levelHandler;
    private ConstraintLayout finalLa;


    private void restart(){


        interval = 100;
        fuelCount = 100;
        life = 4;
        scoreTv = 0;
        roadSpeed = 10f;
        fuelTankInterval = 500;
        fuelTankTimeLeft = fuelTankInterval;
        fuelTranslationY = fuelAppearTranslation;
        fuelTranslationX = 0f;
        coinTranslationY = coinAppearTranslation;
        coinTranslationX = 0f;
        spikeTranslationY = spikeAppearTranslation;
        spikeTranslationX = 0f;

        roadTranslation = 0f;
        carTranslation = 0f;

        fuelImage.setTranslationX( outOfScreenTranslation);
        fuelImage.setTranslationY(fuelTranslationY);
        coinImg.setTranslationX(outOfScreenTranslation);
        coinImg.setTranslationY(coinTranslationY);
        spikeImg.setTranslationX(outOfScreenTranslation);
        spikeImg.setTranslationY(spikeTranslationY);

        finalLa.setVisibility(View.INVISIBLE);
        carImg.setTranslationX(carTranslation);


        try{
            FileInputStream maxScoreFile;
            maxScoreFile = openFileInput(maxScoreFileName);
            DataInputStream reader = new DataInputStream(maxScoreFile);
            maxScore = reader.readInt();
            reader.close();
            maxScoreFile.close();
        }
        catch (Exception ex){
            try{
                saveMaxScore();
            }catch (Exception ex2){
                maxScore = -1;
            }
        }

        //timer.schedule(timerTick, 1000);
        levelHandler.postDelayed(timerTick, 30);


    }

    private  void saveMaxScore() throws IOException {
        FileOutputStream newScoreFile = openFileOutput(maxScoreFileName, Context.MODE_PRIVATE);
        DataOutputStream writer = new DataOutputStream(newScoreFile);
        writer.writeInt(maxScore);
        writer.close();
        newScoreFile.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

        levelHandler = new Handler();
        rnd = new Random();



        alphaTransition = AnimationUtils.loadAnimation(this, R.anim.alpha);
        alphaTransition.reset();

        scaleTransition = AnimationUtils.loadAnimation(this, R.anim.scaler);
        scaleTransition.reset();



        findViewById(R.id.imageRestart).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        restart();
                    }
                }
        );




        final TextView speedScore = (findViewById(R.id.textSpeed));
        speedScore.setText("Speed - " + roadSpeed);

        final TextView fuelScore = findViewById(R.id.tvText);
        fuelScore.setText("Fuel - " + fuelCount);

        final TextView Score = findViewById(R.id.textScore);
        Score.setText("Score - " + scoreTv);

        final TextView health = findViewById(R.id.textHealth);
        health.setText("Health - " + life);

        roadImg =  findViewById(R.id.imageRoad);
        carImg = findViewById(R.id.imageCar);
        fuelImage = findViewById(R.id.imageFuel);
        spikeImg = findViewById(R.id.imageSpike);
        coinImg = findViewById(R.id.imageCoin);
        finalLa = findViewById(R.id.gameOver);




        fuel = new Runnable() {
            @Override
            public void run() {
                fuelScore.setText("Fuel - " + fuelCount);
            }
        };

        speed = new Runnable() {
            @Override
            public void run() {
                speedScore.setText("Speed - " + roadSpeed);
            }
        };

        score = new Runnable() {
            @Override
            public void run() {
                Score.setText("Score - " + scoreTv);
            }
        };

        Health = new Runnable() {
            @Override
            public void run() {
                health.setText("Health - " + life);
            }
        };

        findViewById(R.id.mainLa)
                .setOnTouchListener(new SwipeListener(level.this){
                    @Override
                    public void onSwipeRight(){
                        if(carTranslation <= 0f){

                            carTranslation += 350f;
                            carImg.setTranslationX(
                                    carTranslation
                            );
                        }
                    }

                    @Override
                    public void onSwipeLeft(){
                        if(carTranslation >= 0f){

                            carTranslation -= 350f;
                            carImg.setTranslationX(
                                    carTranslation
                            );
                        }
                    }
                });

        timerTick = new TimerTask() {
            @Override
            public void run() {

                cnt++;
                if(cnt % 25 == 1 ) fuelCount--;

                if(scoreTv > maxScore) {
                    maxScore = scoreTv;
                    try {
                        saveMaxScore();
                    } catch (IOException ex) {

                    }
                }

                if(fuelCount <= 0 || life <=0){
                    levelHandler.post(
                            new Runnable(){
                        @Override
                        public void run(){

                            finalLa.setVisibility(View.VISIBLE);

                            TextView scoreNow = findViewById(R.id.textScoreNow);
                            scoreNow.setText("Score - " + scoreTv);
                            TextView scoreBest  = findViewById(R.id.textScoreBest);
                            scoreBest.setText("Best score - " + maxScore);


                        }
                    });
                }
                else{
                  //  timer.schedule(timerTick, 30);
                    levelHandler.postDelayed(timerTick, 30);
                }

                //road
                level.this.runOnUiThread(fuel);
                level.this.runOnUiThread(speed);
                level.this.runOnUiThread(score);
                level.this.runOnUiThread(Health);

                roadTranslation += roadSpeed;

                if(roadTranslation > 408f){
                    roadTranslation = 0f;
                }

                roadImg.setTranslationY(roadTranslation);

                //fuel appearance and moving
                if(interval > 0) { //pause after app start
                    --interval;
                } else{
                    // fuel tank
                    fuelTankTimeLeft -= roadSpeed / 5 ;
                    if( fuelTankTimeLeft <= 0) {  // tank appearance and moving
                        if( fuelTranslationY == fuelAppearTranslation ) {
                            fuelTranslationX =  // tank appearance - moving to screen
                                    350 * ( 1 - rnd.nextInt( 3 ) ) ;
                            fuelTranslationY += roadSpeed ;
                            fuelTankTimeLeft = fuelTankInterval ;
                            // if close to spike
                            if( fuelTranslationX == spikeTranslationX ) {
                                if( Math.abs( fuelTranslationY - spikeTranslationY ) < 20 ) {
                                    fuelTranslationY -= 100 ;
                                }
                            }
                            if( fuelTranslationX == coinTranslationX ) {
                                if( Math.abs( fuelTranslationY - coinTranslationY ) < 20 ) {
                                    fuelTranslationY -= 100 ;
                                }
                            }
                            fuelImage.setTranslationX( fuelTranslationX ) ;
                        } else {
                            fuelTranslationY += roadSpeed ;
                            if( fuelTranslationY > 1700f ) {  // tank disappearance
                                fuelImage.setTranslationX( outOfScreenTranslation ) ;
                                fuelTranslationY = fuelAppearTranslation ;

                            }
                            fuelImage.setTranslationY( fuelTranslationY ) ;
                        }
                    }
                    // spike appearance
                    if(spikeTranslationY == spikeAppearTranslation){
                        spikeTranslationX =  //spike appearance - moving to screen
                                350 * (1 - rnd.nextInt(3));
                        spikeTranslationY += roadSpeed;

                        if( coinTranslationX == spikeTranslationX ) {
                            if( Math.abs( spikeTranslationY - coinTranslationY ) < 20 ) {
                                spikeTranslationY -= 100 ;
                            }
                        }
                        if( spikeTranslationX == fuelTranslationX ) {
                            if( Math.abs( spikeTranslationY - fuelTranslationY ) < 20 ) {
                                spikeTranslationY -= 100 ;
                            }
                        }

                        spikeImg.setTranslationX( spikeTranslationX );
                    }
                    else{
                        spikeTranslationY += roadSpeed;
                        if(spikeTranslationY > 1700f){  //spike disappearance
                            spikeImg.setTranslationX( outOfScreenTranslation );
                            spikeTranslationY = spikeAppearTranslation;
                        }
                        spikeImg.setTranslationY(spikeTranslationY);
                    }
                    // coin appearance
                    if(coinTranslationY == coinAppearTranslation ){
                        coinTranslationX =  //coin appearance - moving to screen
                                350  * (1 - rnd.nextInt(3));
                        coinTranslationY += roadSpeed;

                        if( coinTranslationX == fuelTranslationX ) {
                            if( Math.abs( coinTranslationY - fuelTranslationY ) < 20 ) {
                                coinTranslationY -= 100 ;
                            }
                        }
                        if( coinTranslationX == spikeTranslationX ) {
                            if( Math.abs( coinTranslationY - spikeTranslationY ) < 20 ) {
                                coinTranslationY -= 100 ;
                            }
                        }

                        coinImg.setTranslationX( coinTranslationX );
                    }
                    else{
                        coinTranslationY += roadSpeed;
                        if(coinTranslationY > 1700f){  //coin disappearance
                            coinImg.setTranslationX( outOfScreenTranslation );
                            coinTranslationY = coinAppearTranslation;
                        }
                        coinImg.setTranslationY(coinTranslationY);
                    }
                }

                //fuel - car collisions
                if(fuelTranslationY > 1150 && fuelTranslationY < 1900f ){
                    if( carTranslation < 0 && fuelTranslationX < 0  //car & fuel in left row
                            || carTranslation > 0 && fuelTranslationX > 0  //car & fuel in right row
                            || carTranslation == 0 && fuelTranslationX == 0  //car & fuel in center row
                    ){
                        //collision detected
                        //fuel disappearance
                        fuelTranslationX = outOfScreenTranslation;
                        fuelTranslationY = fuelAppearTranslation;
                        fuelImage.setTranslationY(fuelTranslationY);
                        fuelImage.setTranslationX(fuelTranslationX);

                        //message

                        fuelCount += 50;
                    }
                }

                //coin - car collisions
                if(coinTranslationY > 1150 && coinTranslationY < 1900f ){
                    if( carTranslation < 0 && coinTranslationX < 0  //car & coin in left row
                            || carTranslation > 0 && coinTranslationX > 0  //car & coin in right row
                            || carTranslation == 0 && coinTranslationX == 0  //car & coin in center row
                    ){
                        //collision detected
                        //fuel disappearance
                        coinTranslationX = outOfScreenTranslation;
                        coinTranslationY = coinAppearTranslation;
                        coinImg.setTranslationY(coinTranslationY);
                        coinImg.setTranslationX(coinTranslationX);

                        //message

                        roadSpeed ++;
                        scoreTv += 10;
                    }
                }

                //spike - car collisions
                if(spikeTranslationY > 1150 && spikeTranslationY < 1900f ){
                    if( carTranslation < 0 && spikeTranslationX < 0  //car & spike in left row
                            || carTranslation > 0 && spikeTranslationX > 0  //car & spike in right row
                            || carTranslation == 0 && spikeTranslationX == 0  //car & spike in center row
                    ){
                        //collision detected
                        //spike disappearance
                        spikeTranslationX = outOfScreenTranslation;
                        spikeTranslationY = spikeAppearTranslation;
                        spikeImg.setTranslationY(spikeTranslationY);
                        spikeImg.setTranslationX(spikeTranslationX);

                        carImg.startAnimation(alphaTransition);
                        carImg.startAnimation(scaleTransition);

                        //message
                        life--;



                    }
                }

            }
        };
        restart();
    }
}
