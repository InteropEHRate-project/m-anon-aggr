package eu.interopehrate.rdsanoni;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

public class RetrievePseudonym implements Callable<String> {

    String anAssertion;
    String publicKey;

    public RetrievePseudonym(String anAssertion, String publicKey){
        this.anAssertion = anAssertion;
        this.publicKey = publicKey;
    }

    @Override
    public String call() throws Exception {
        String json = new JSONObject()
                .put("assertion", anAssertion)
                .put("pubkey", publicKey)
                .toString();
        try {
            String accessCondition = request(json, "http://interoperate-ejbca-service.euprojects.net/idpaccesscondition");
            json = new JSONObject()
                    .put("accessCondition", accessCondition)
                    .toString();
            String keyPair = request(json, "http://interoperate-idp-service.euprojects.net/requestpseudonym");
            return keyPair;
        } catch (IOException e) {
            e.printStackTrace();
            JSONObject message = new JSONObject();
            message.put("message", "The pseudonym could not be retrieved from the Pseudonym Provider.");
            return message.toString();
        }
    }

    private String request(String json, String currentUrl) throws IOException {

        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL(currentUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            OutputStream outputStream = connection.getOutputStream();
            byte[] input = json.getBytes("UTF-8");
            outputStream.write(input);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.close();

            connection.connect();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String responseLine = null;
            while ((responseLine = bufferedReader.readLine()) != null) {
                response.append(responseLine.trim());
                //System.out.println(responseLine.trim());
            }
            bufferedReader.close();
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response.toString();
    }

}