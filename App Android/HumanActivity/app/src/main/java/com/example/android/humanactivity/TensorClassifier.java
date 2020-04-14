package com.example.android.humanactivity;

import android.content.Context;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

public class TensorClassifier {

    static {
        System.loadLibrary("tensorflow_inference");
    }

    private TensorFlowInferenceInterface tensorInterface;
    private static final String MODEL_FILE = "file:///android_asset/freeze_model1.pb";
    private static final String INPUT = "input";
    private static final String[] OUTPUTS = {"y"};
    private static final String OUTPUT = "y";
    private static final long[] SIZE = {1, 200, 3};
    private static final int OUTSIZE = 6;

    public TensorClassifier(final Context context)
    {
        tensorInterface = new TensorFlowInferenceInterface(context.getAssets(),MODEL_FILE);
    }

    float[] predict(float[] window_data)
    {
        float prediction[] = new float[OUTSIZE];
        tensorInterface.feed(INPUT,window_data,SIZE);
        tensorInterface.run(OUTPUTS);
        tensorInterface.fetch(OUTPUT,prediction);
        return prediction;

    }
}
