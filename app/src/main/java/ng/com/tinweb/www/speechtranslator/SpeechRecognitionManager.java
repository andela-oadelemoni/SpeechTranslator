package ng.com.tinweb.www.speechtranslator;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;

import ng.com.tinweb.www.speechtranslator.databinding.ActivityMainBinding;

/**
 * Created by kamiye on 07/08/2016.
 */
public class SpeechRecognitionManager implements RecognitionListener {


    private static final String LOG_TAG = "VOICE";
    private ActivityMainBinding binding;
    private Processor speechProcessor;

    public SpeechRecognitionManager(ActivityMainBinding binding, Processor processor) {
        this.binding = binding;
        this.speechProcessor = processor;
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
        binding.speech.setText("Speech Ended!");
    }

    @Override
    public void onError(int error) {
        String errorMessage = getErrorText(error);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
        binding.speech.setText(errorMessage);
    }

    @Override
    public void onResults(Bundle results) {
        speechProcessor.processResult(results);
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

    public interface Processor {
        void processResult(Bundle results);
    }
}
