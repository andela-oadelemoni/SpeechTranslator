package ng.com.tinweb.www.speechtranslator;

import android.app.Application;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;

/**
 * Created by kamiye on 05/08/2016.
 */
public class SpeechTranslatorApplication extends Application {

    private static final String PUBNUB_CHANNEL = "IOTHack";
    private static final String PUBNUB_SUB_KEY = "sub-c-2e7ff474-5863-11e5-b018-0619f8945a4f";
    private static final String PUBNUB_PUB_KEY = "pub-c-8ae56e53-70e7-4898-9595-2f8a57ba364a";

    private static PubNub pubNub;
    @Override
    public void onCreate() {
        super.onCreate();

        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey(PUBNUB_SUB_KEY);
        pnConfiguration.setPublishKey(PUBNUB_PUB_KEY);
        //pnConfiguration.setSecretKey("sec-c-YjczYmYxYzYtODM4Ny00NWQ4LTljNzAtMjg3NTZjNDYxNDUw");

        // production keys when ready
        /*
          publish key: pub-c-8ae56e53-70e7-4898-9595-2f8a57ba364a
          subscribe key: sub-c-2e7ff474-5863-11e5-b018-0619f8945a4f
          channel: IOTHack
         */

        pubNub = new PubNub(pnConfiguration);
    }

    public static PubNub getPubNubObject() {
        return pubNub;
    }

    public static String getPubnubChannel() {
        return PUBNUB_CHANNEL;
    }
}
