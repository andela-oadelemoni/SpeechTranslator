package ng.com.tinweb.www.speechtranslator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ng.com.tinweb.www.speechtranslator.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements View.OnLongClickListener,
        View.OnTouchListener, SpeechRecognitionManager.Processor {

    private static final int PERMISSION_REQUEST_CODE = 7;

    private SpeechRecognizer speechRecognizer;
    private SpeechRecognitionManager recognitionManager;
    private Intent recognizerIntent;
    private String LOG_TAG = "Voice";

    private ActivityMainBinding binding;
    private boolean isKeyPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        checkAudioPermission();

        binding.button.setOnLongClickListener(this);
        binding.button.setOnTouchListener(this);

        binding.speechBar.setVisibility(View.INVISIBLE);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());

        recognitionManager = new SpeechRecognitionManager(binding, this);
        speechRecognizer.setRecognitionListener(recognitionManager);

        // RECOGNIZER INTENTS
        setupRecognizerIntent();
    }

    private void checkAudioPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            requestContactsPermission();
        }
    }

    private void requestContactsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(this, "Application Needs Audio Permission", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (!(requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            finish();
        }
    }

    private void setupRecognizerIntent() {
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
        speechRecognizer.setRecognitionListener(recognitionManager);
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
            toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
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
    public void processResult(Bundle results) {
        Log.i(LOG_TAG, "processResultsCalled");

        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = matches.get(0);

        try {
            processText(text);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        binding.speech.setText(text);
    }

    private void processText(String text) throws JSONException {
        String decodedText = text.toLowerCase();
        JSONObject jsonObject = new JSONObject();
        if (decodedText.matches("(.*)bulb(.*)off(.*)")) {
            jsonObject.put("command", "bulb off");
        }
        else if (decodedText.matches("(.*)bulb(.*)on(.*)")) {
            jsonObject.put("command", "bulb on");
        }
        else if (decodedText.matches("(.*)fan(.*)off(.*)")) {
            jsonObject.put("command", "fan off");
        }
        else if (decodedText.matches("(.*)fan(.*)on(.*)")) {
            jsonObject.put("command", "fan on");
        }
        else {
            jsonObject.put("command", "invalid command received");
        }
        publishMessage(jsonObject);
    }

    private void publishMessage(JSONObject message) throws JSONException {
        if (message.length() > 0) {
            PubNub pubNub = SpeechTranslatorApplication.getPubNub();
            pubNub.publish()
                    .message(message.get("command"))
                    .channel("Channel-12smb62lc")
                    .shouldStore(true)
                    .usePOST(true)
                    .async(new PNCallback<PNPublishResult>() {
                        @Override
                        public void onResponse(PNPublishResult result, PNStatus status) {
                            // handle publish result, status always present, result if successful
                            // status.isError to see if error happened
                            if (status.isError()) {
                                Toast.makeText(MainActivity.this, "Error!! "+status.getErrorData().getInformation(), Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(MainActivity.this, "Successful ", Toast.LENGTH_LONG).show();
                            }

                        }
                    });
        }
    }


}
