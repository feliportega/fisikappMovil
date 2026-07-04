package com.marcos.fisikappmovil.ui.UnityAR;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.unity3d.player.UnityPlayerActivity;

public class UnityArActivity extends UnityPlayerActivity {

    private static final String TAG = "UnityArActivity";

    public static final String EXTRA_EXERCISE_DATA = "exerciseData";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String exerciseJson = getIntent().getStringExtra(EXTRA_EXERCISE_DATA);

        Log.d(TAG, "UnityArActivity creada. JSON recibido: " + exerciseJson);
    }

    public void onUnityLabFinished(String resultJson) {
        android.util.Log.d("UnityArActivity", "Resultado recibido desde Unity: " + resultJson);

        android.content.Intent intent = new android.content.Intent(
                this,
                ResultadoUnityActivity.class
        );

        intent.putExtra(ResultadoUnityActivity.EXTRA_UNITY_RESULT, resultJson);

        startActivity(intent);
        finish();
    }
}
