package ng.com.tinweb.www.speechtranslator;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import ng.com.tinweb.www.speechtranslator.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements View.OnLongClickListener,
        View.OnTouchListener, RecognitionListener {


    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private String LOG_TAG = "Voice";

    private ActivityMainBinding binding;
    private boolean isKeyPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.button.setOnLongClickListener(this);
        binding.button.setOnTouchListener(this);

        binding.speechBar.setVisibility(View.INVISIBLE);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        speechRecognizer.setRecognitionListener(this);

        // RECOGNIZER INTENTS
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }


    @Override
    protected void onResume() {
        super.onResume();
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        speechRecognizer.setRecognitionListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            Log.i(LOG_TAG, "destroy");
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == binding.button.getId()) {
            isKeyPressed = !isKeyPressed;
            ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
            toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP,150);
            startListeningForSpeech();
        }
        return true;
    }

    private void startListeningForSpeech() {
        binding.speechBar.setVisibility(View.VISIBLE);
        binding.speechBar.setIndeterminate(true);
        binding.text.setText(R.string.listening_action);
        speechRecognizer.startListening(recognizerIntent);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.onTouchEvent(event);
        if (isKeyPressed && event.getAction() == MotionEvent.ACTION_UP) {
            isKeyPressed = !isKeyPressed;
            Toast.makeText(this, "Listening stopped", Toast.LENGTH_SHORT).show();
            stopListeningForSpeech();
        }
        return true;
    }

    private void stopListeningForSpeech() {
        binding.speechBar.setIndeterminate(false);
        binding.speechBar.setVisibility(View.INVISIBLE);
        binding.text.setText(R.string.button_description);
        speechRecognizer.stopListening();
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.i(LOG_TAG, "onReadyForSpeech");
        binding.speech.setText(R.string.translating);
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
        binding.speechBar.setIndeterminate(false);
        binding.speechBar.setMax(10);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        binding.speechBar.setProgress((int) rmsdB);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        binding.speechBar.setIndeterminate(true);
    }

    @Override
    public void onError(int error) {
        String errorMessage = getErrorText(error);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
        binding.speech.setText(R.string.friendly_error);
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");

        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = matches.get(0);
        binding.speech.setText(text);
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        Log.i(LOG_TAG, "onEvent");
    }

    private String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

}
