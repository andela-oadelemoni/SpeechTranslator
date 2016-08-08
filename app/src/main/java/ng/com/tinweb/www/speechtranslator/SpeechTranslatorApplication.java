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
        pnConfiguration.setSubscribeKey("sub-c-8b565b64-5b16-11e6-9f02-0619f8945a4f");
        pnConfiguration.setPublishKey("pub-c-ca0be123-2642-4f78-aa87-25e32254d9f4");
        pnConfiguration.setSecretKey("sec-c-YjczYmYxYzYtODM4Ny00NWQ4LTljNzAtMjg3NTZjNDYxNDUw");

        pubNub = new PubNub(pnConfiguration);
    }

    public static PubNub getPubNub() {
        return pubNub;
    }
}
