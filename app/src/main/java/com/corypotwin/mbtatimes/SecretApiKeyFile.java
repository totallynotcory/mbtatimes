package com.corypotwin.mbtatimes;

/**
 * Created by ctpotwin on 8/7/16.
 */
public class SecretApiKeyFile {
    //  Note:  This is the free public key and is not allow to be used for publication.
    //      It was acquired here at the following link and if it's not working, see if it
    //      needs to be updated: http://realtime.mbta.com/Portal/Content/Download/APIKey.txt
    private static String publicApiKey = "wX9NwuHnZU2ToO7GmGR9uw";

    public static String getKey(){

        return publicApiKey;
    }
}