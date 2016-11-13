package io.github.benoitduffez.cupsprint.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import io.github.benoitduffez.cupsprint.CupsPrintApp;
import io.github.benoitduffez.cupsprint.app.HostNotVerifiedActivity;

/**
 * Used with {@link HostNotVerifiedActivity} to trust certain hosts
 */
public class AndroidCupsHostnameVerifier implements HostnameVerifier {
    @Override
    public boolean verify(String hostname, SSLSession session) {
        return HostNotVerifiedActivity.isHostnameTrusted(CupsPrintApp.getContext(), hostname);
    }
}
