package com.example.android.humanactivity;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.speech.tts.TextToSpeech;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    SensorManager manager;
    Sensor accelerometer;
    final int sample_size = 200;
    List<Float> x;
    List<Float> y;
    List<Float> z;
    TensorClassifier tensorClassifier;
    TextView tv;
    TextView prob;
    ConstraintLayout layout;
    TextToSpeech tospeech;
    int res;
    Switch speech;
    Boolean check;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.sample);
        prob = (TextView) findViewById(R.id.prob);
        speech = (Switch) findViewById(R.id.speech);
        check = speech.isChecked();
        layout = (ConstraintLayout) findViewById(R.id.layout);

        x = new ArrayList<>();
        y = new ArrayList<>();
        z = new ArrayList<>();
        manager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        tensorClassifier = new TensorClassifier(getApplicationContext());

        manager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_GAME);

        tospeech = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS)
                {
                    res = tospeech.setLanguage(Locale.UK);
                }
                else
                {
                    Toast.makeText(MainActivity.this,"TTS is not supported",Toast.LENGTH_SHORT).show();
                }
            }
        });

        speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(speech.isChecked())
                {
                    Toast.makeText(MainActivity.this,"Speech turned on",Toast.LENGTH_SHORT).show();

                }
                else
                {
                    Toast.makeText(MainActivity.this,"Speech turned off",Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        manager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        predict_now();
        x.add(event.values[0]);
        y.add(event.values[1]);
        z.add(event.values[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    void predict_now()
    {
        if(x.size()==sample_size && y.size()==sample_size && z.size()==sample_size)
        {
            Toast.makeText(MainActivity.this,"Classifying!!",Toast.LENGTH_SHORT).show();
            List<Float> window = new ArrayList<>();
            window.addAll(x);
            window.addAll(y);
            window.addAll(z);
            int count = 0 ;
            float[] data = new float[window.size()];

            for(Float i : window)
            {
                data[count++] = (i!=null?i:Float.NaN);
            }

            float[] results = tensorClassifier.predict(data);

            float max = -1;
            int idx = 0;
            //Toast.makeText(MainActivity.this,"Got Results!!",Toast.LENGTH_SHORT).show();
            for(int i=0;i<results.length;i++)
            {
                if(max<results[i])
                {
                    max= results[i];
                    idx = i;
                }
            }
            Toast.makeText(MainActivity.this,"View Changing",Toast.LENGTH_SHORT).show();
            String[] classes = {"Downstairs", "Jogging", "Sitting", "Standing", "Upstairs", "Walking"};
            tv.setText(classes[idx]+"!");
            String str = results[idx]+"";
            prob.setText(str);
            check = speech.isChecked();

            String spStr = "You're "+classes[idx];
            if(check)
            {
                tospeech.speak(spStr,TextToSpeech.QUEUE_FLUSH,null);
            }

            x.clear();
            y.clear();
            z.clear();

        }

    }
}
