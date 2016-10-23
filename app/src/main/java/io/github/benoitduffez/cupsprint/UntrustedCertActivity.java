package io.github.benoitduffez.cupsprint;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jonbanjo.ssl.JfSSLScheme;

import java.security.cert.X509Certificate;

/**
 * Show an untrusted cert info + two buttons to accept or refuse to trust said cert
 */
public class UntrustedCertActivity extends Activity {
    public static final String KEY_CERT = UntrustedCertActivity.class.getName() + ".Certs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.untrusted_cert);

        final X509Certificate cert = (X509Certificate) getIntent().getSerializableExtra(KEY_CERT);

        // Build short cert description
        StringBuilder sb = new StringBuilder();
        sb.append("Issuer: ").append(cert.getIssuerX500Principal().toString());
        sb.append("\nValidity: not before ").append(cert.getNotBefore().toString());
        sb.append("\nValidity: not after ").append(cert.getNotAfter().toString());
        sb.append("\nSubject: ").append(cert.getSubjectX500Principal().getName());
        sb.append("\nKey algo: ").append(cert.getSigAlgName());
        sb.append("\nKey: ").append(cert.getPublicKey().toString());

        TextView certInfo = (TextView) findViewById(R.id.untrusted_certinfo);
        certInfo.setText(sb);

        findViewById(R.id.untrusted_trust_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (JfSSLScheme.saveCertificates(new X509Certificate[]{cert})) {
                    Toast.makeText(UntrustedCertActivity.this, R.string.untrusted_trusted, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(UntrustedCertActivity.this, R.string.untrusted_couldnt_trust, Toast.LENGTH_LONG).show();
                }
                finish();
            }
        });

        findViewById(R.id.untrusted_abort_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
