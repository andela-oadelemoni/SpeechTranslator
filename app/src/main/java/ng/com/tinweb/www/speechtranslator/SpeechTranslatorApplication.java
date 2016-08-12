package ng.com.tinweb.www.speechtranslator;

import android.app.Application;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;

/**
 * Created by kamiye on 05/08/2016.
 */
public class SpeechTranslatorApplication extends Application {

    private static PubNub pubNub;
    @Override
    public void onCreate() {
        super.onCreate();

        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey("sub-c-2e7ff474-5863-11e5-b018-0619f8945a4f");
        pnConfiguration.setPublishKey("pub-c-8ae56e53-70e7-4898-9595-2f8a57ba364a");
        //pnConfiguration.setSecretKey("sec-c-YjczYmYxYzYtODM4Ny00NWQ4LTljNzAtMjg3NTZjNDYxNDUw");

        pubNub = new PubNub(pnConfiguration);
    }

    public static PubNub getPubNub() {
        return pubNub;
    }
}
