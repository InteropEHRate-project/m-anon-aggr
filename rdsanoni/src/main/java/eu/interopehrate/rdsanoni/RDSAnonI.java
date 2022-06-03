package eu.interopehrate.rdsanoni;

import android.content.Context;

import org.json.JSONException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.ExecutionException;

public interface RDSAnonI {

    public void setPseudo(String pseudoType, String pseudo, String studyID, Context context);
    public String getPseudo(String studyID, Context context);
    public String pseudonymizeData(String data, String fileType, String studyID, Context context) throws JSONException, ExecutionException, InterruptedException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyStoreException, SignatureException, InvalidKeyException, InvalidKeySpecException;
    public String retrievePseudonym(String anAssertion, String publicKey) throws IOException, InterruptedException, ExecutionException, JSONException;

    public String anonymizeData(String data, String fileType) throws JSONException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyStoreException, SignatureException, InvalidKeyException, InvalidKeySpecException;

}
