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
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ng.com.tinweb.www.speechtranslator.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements View.OnLongClickListener,
        View.OnTouchListener, SpeechRecognitionManager.Processor {

    private static final int PERMISSION_REQUEST_CODE = 7;

    private static final String JSON_KEY = "command";

    private SpeechRecognizer speechRecognizer;
    private SpeechRecognitionManager recognitionManager;
    private Intent recognizerIntent;
    private String LOG_TAG = "Voice";

    private PubNub pubNub;

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

        // set pubnub object;
        pubNub = SpeechTranslatorApplication.getPubNubObject();
        subscribeToPubNub();
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
            jsonObject.put(JSON_KEY, "bulb off");
        }
        else if (decodedText.matches("(.*)bulb(.*)on(.*)")) {
            jsonObject.put(JSON_KEY, "bulb on");
        }
        else if (decodedText.matches("(.*)fan(.*)off(.*)")) {
            jsonObject.put(JSON_KEY, "fan off");
        }
        else if (decodedText.matches("(.*)fan(.*)on(.*)")) {
            jsonObject.put(JSON_KEY, "fan on");
        }
        else {
            jsonObject.put(JSON_KEY, "invalid command received");
        }
        publishMessage(jsonObject);
    }

    private void publishMessage(JSONObject message) throws JSONException {
        if (message.length() > 0) {
            pubNub.publish()
                    .message(message.get(JSON_KEY))
                    .channel(SpeechTranslatorApplication.getPubnubChannel())
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

    private void subscribeToPubNub() {
        SubscribeCallback subscribeCallback = new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
                // the status object returned is always related to subscribe but could contain
                // information about subscribe, heartbeat, or errors
                // use the operationType to switch on different options
                switch (status.getOperation()) {
                    // let's combine unsubscribe and subscribe handling for ease of use
                    case PNSubscribeOperation:
                    case PNUnsubscribeOperation:
                        // note: subscribe statuses never have traditional
                        // errors, they just have categories to represent the
                        // different issues or successes that occur as part of subscribe
                        switch(status.getCategory()) {
                            case PNConnectedCategory:
                                // this is expected for a subscribe, this means there is no error or issue whatsoever
                            case PNReconnectedCategory:
                                // this usually occurs if subscribe temporarily fails but reconnects. This means
                                // there was an error but there is no longer any issue
                            case PNDisconnectedCategory:
                                // this is the expected category for an unsubscribe. This means there
                                // was no error in unsubscribing from everything
                            case PNUnexpectedDisconnectCategory:
                                // this is usually an issue with the internet connection, this is an error, handle appropriately
                                // retry will be called automatically
                            case PNAccessDeniedCategory:
                                // this means that PAM does allow this client to subscribe to this
                                // channel and channel group configuration. This is another explicit error
                            default:
                                // More errors can be directly specified by creating explicit cases for other
                                // error categories of `PNStatusCategory` such as `PNTimeoutCategory` or `PNMalformedFilterExpressionCategory` or `PNDecryptionErrorCategory`
                        }

                    case PNHeartbeatOperation:
                        // heartbeat operations can in fact have errors, so it is important to check first for an error.
                        // For more information on how to configure heartbeat notifications through the status
                        // PNObjectEventListener callback, consult <link to the PNCONFIGURATION heartbeart config>
                        if (status.isError()) {
                            // There was an error with the heartbeat operation, handle here
                        } else {
                            // heartbeat operation was successful
                        }
                    default: {
                        // Encountered unknown status type
                    }
                }
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                final String status = message.getMessage().get(JSON_KEY).textValue();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showReturnMessage(status);
                    }
                });
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {

            }
        };
        pubNub.addListener(subscribeCallback);

        List<String> channels = new ArrayList<>();
        channels.add(SpeechTranslatorApplication.getPubnubChannel());
        pubNub.subscribe()
                .channels(channels)
                .execute();
    }

    private void showReturnMessage(String message) {
        String[] returnMessages = getResources().getStringArray(R.array.return_messages);
        List<String> messagesList = new ArrayList<>(Arrays.asList(returnMessages));

        if (messagesList.contains(message)) {
            binding.status.setText(message);
        }
    }


}
