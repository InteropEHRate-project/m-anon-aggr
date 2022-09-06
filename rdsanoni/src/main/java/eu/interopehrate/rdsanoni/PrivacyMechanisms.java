package eu.interopehrate.rdsanoni;

import android.content.Context;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.interopehrate.m_rds_sm.CryptoManagementFactory;
import eu.interopehrate.m_rds_sm.api.*;

public class PrivacyMechanisms implements RDSAnonI {

    private static PrivacyMechanisms singlePrivacyMechanismsInstance = null;

    private String pseudo;
    private String pseudoType;
    private String studyID;
    private Context context;

    private static String randomValue = "";
    private static String mechanism = "";
    private static String profile = "";
    private static final ArrayList<String> originalAttributes = new ArrayList<>(Arrays.asList("text", "entry"));
    private static final ArrayList<String> temporaryAttributes = new ArrayList<>(Arrays.asList("keepTextAttribute", "keepEntryAttribute"));
    private static HashMap<String, String> anonDataPerResource = new LinkedHashMap<>();
    private static HashMap<String, String> ids = new LinkedHashMap<>();
    private static String currentID = "";
    private static String provenanceExtensionID = "";
    private static String jsonValue = "";
    private static boolean hasProvenanceExtension = false;
    public static final String CA_URL = "http://interoperate-ejbca-service.euprojects.net";

    public PrivacyMechanisms(Context context){
        this.pseudo = "";
        this.pseudoType = "";
        this.studyID = "";
        this.context = context;
    }

    public PrivacyMechanisms(String pseudo, String pseudoType, String studyID, Context context){
        this.pseudo = pseudo;
        this.pseudoType = pseudoType;
        this.studyID = studyID;
        this.context = context;
    }

    public Context getContext() {
        return this.context;
    }

    public static PrivacyMechanisms PrivacyMechanisms(Context context){
        if(singlePrivacyMechanismsInstance == null){
            singlePrivacyMechanismsInstance = new PrivacyMechanisms(context);
        }

        return singlePrivacyMechanismsInstance;
    }

    /*The method "setPseudo" takes as input the pseudo, which is either a pseudo-identity or a pseudonym, the
    pseudoType, which indicates whether the pseudo is a pseudo-identity or a pseudonym, and the ID of the study.*/
    public void setPseudo(String pseudoType, String pseudo, String studyID, Context context){
        DBHandler dbHandler = new DBHandler(context);
        dbHandler.insertPseudo(pseudoType, pseudo, studyID);
    }

    /*The method "getPseudo" takes as input the ID of the study, and returns the pseudo-identity or the pseudonym,
    which was assigned to the citizen for the current study.*/
    public String getPseudo(String studyID, Context context){
        DBHandler dbHandler = new DBHandler(context);
        return dbHandler.retrievePseudo(studyID);
    }

    /*The method "pseudonymizeData" takes as input the citizen's data, which should get pseudonymized, along
    with the type of the data file, and returns the pseudonymized dataset.*/
    public String pseudonymizeData(String data, String fileType, String studyID, Context context) throws JSONException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyStoreException, SignatureException, InvalidKeyException, InvalidKeySpecException {
        long startTime = System.nanoTime();
        randomValue = "";
        currentID = "";
        profile = "";
        mechanism = "Pseudonymization";
        anonDataPerResource = new LinkedHashMap<>();
        ids = new LinkedHashMap<>();

        pseudo = getPseudo(studyID, context);

        if(pseudo.equals("")){
            JSONObject message = new JSONObject();
            message.put("message", "Since the pseudo for the current study was not found, the pseudonymization process cannot be implemented.");
            return message.toString();
        } else {
            randomValue = pseudo;
            String pseudonymizedData = "";

            JsonObject jsonObject = JsonParser.parseString(data).getAsJsonObject();
            findElementsChildren(jsonObject, "id");
            //System.out.println(ids.toString());

            if(isBundle(data)){
                pseudonymizedData = anonymizeBundle(data);
            } else {
                String resourceType = getResourceType(data);
                profile = resourceType;
                JSONObject resource = new JSONObject(data);
                currentID = ids.get(resource.get("id").toString());
                pseudonymizedData = anonymizeFHIResource(data, resourceType);
            }

            pseudonymizedData =  pseudonymizedData.replaceAll("\"addNewExtensionHere\":","\"extension\":");
            pseudonymizedData = deleteEmptyPairs(pseudonymizedData);
            pseudonymizedData = beautifyJSON(pseudonymizedData);

            end(startTime);

            return pseudonymizedData;
        }

    }

    /*The method "retrievePseudonym" takes as input the anonymous assertion token and the public key of the user, which are already stored in the
    citizen’s phone, and returns the pseudonym which was retrieved from the Pseudonym Provider.*/
    public String retrievePseudonym(String anAssertion, String publicKey) throws InterruptedException, ExecutionException, JSONException {
        //Performs an HTTP request to the Pseudonym Provider in order to retrieve a pseudonym.
        String pseudonym = "";

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        RetrievePseudonym r = new RetrievePseudonym(anAssertion, publicKey);
        Future<String> future = executorService.submit(r);
        String result = future.get();
        //System.out.println("Key pair (encoded) = " + result);
        String[] response = result.split("\\.");
        String body = response[1];
        byte[] decodedBytes = android.util.Base64.decode(body, 0);
        //byte[] decodedBytes = android.util.Base64.decode(body, Base64.DEFAULT);
        String decodedString = new String(decodedBytes);
        System.out.println("Key pair (decoded) = " + decodedString);
        int start = decodedString.indexOf("\"pk\":\"");
        int end = decodedString.indexOf("\",\"sk\"");
        pseudonym = decodedString.substring(start + 6, end);
        //System.out.println("Pseudonym = " + pseudonym);

        return pseudonym;
    }

    /*The method "anonymizeData" takes as input the citizen's data, which should get anonymized, along
    with the type of the data file, and returns the anonymized dataset.*/
    public String anonymizeData(String data, String fileType) throws JSONException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyStoreException, SignatureException, InvalidKeyException, InvalidKeySpecException {
        //System.out.println(data);

        long startTime = System.nanoTime();
        randomValue = "";
        currentID = "";
        profile = "";
        mechanism = "Anonymization";
        anonDataPerResource = new LinkedHashMap<>();
        ids = new LinkedHashMap<>();

        JsonObject jsonObject = JsonParser.parseString(data).getAsJsonObject();
        findElementsChildren(jsonObject, "id");
        //System.out.println(ids.toString());

        String anonymizedData = "";
        if(isBundle(data)){
            anonymizedData = anonymizeBundle(data);
        } else {
            String resourceType = getResourceType(data);
            profile = resourceType;
            JSONObject resource = new JSONObject(data);
            currentID = ids.get(resource.get("id").toString());
            anonymizedData = anonymizeFHIResource(data, resourceType);
        }

        anonymizedData =  anonymizedData.replaceAll("\"addNewExtensionHere\":","\"extension\":");
        anonymizedData = deleteEmptyPairs(anonymizedData);
        anonymizedData = beautifyJSON(anonymizedData);

        end(startTime);

        return anonymizedData;
    }

    private String deleteEmptyPairs(String anonymizedData) {
        String brackets = "\\{"+"\\}";
        anonymizedData = anonymizedData.replaceAll(brackets, String.valueOf(JSONObject.NULL));
        anonymizedData = beautifyJSON(anonymizedData);
        return anonymizedData;
    }

    private String anonymizeFHIResource(String structuredData, String resourceType) throws JSONException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyStoreException, SignatureException, InvalidKeyException, InvalidKeySpecException {

        JsonObject jsonObject = JsonParser.parseString(structuredData).getAsJsonObject();
        if(Attributes.attributesPerProfile.containsKey(resourceType)) {
            for (String currentKey : Attributes.attributesPerProfile.get(resourceType)) {
                deleteAttributes(currentKey, jsonObject);
            }
        } else {
            for (String currentKey : Attributes.attributesPerProfile.get("Other")) {
                deleteAttributes(currentKey, jsonObject);
            }
        }

        String anonymizedData = jsonObject.toString();

        if(!resourceType.contains("Bundle") && !resourceType.equals("Attachment")) {
            anonymizedData = addExtension(anonymizedData);
        }

        if(resourceType.equals("Composition")) {
            anonymizedData = renameAttributesInComposition(anonymizedData, temporaryAttributes, originalAttributes);
        }

        if(hasProvenanceExtension) {
            anonymizedData = appendProvenanceExtension(anonymizedData);
        }

        anonymizedData = beautifyJSON(anonymizedData);

        return anonymizedData;
    }

    private void deleteAttributes(String key, JsonElement jsonElement) throws JSONException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyStoreException, SignatureException, InvalidKeyException, InvalidKeySpecException {

        if (jsonElement.isJsonArray()) {
            for (JsonElement currentElement : jsonElement.getAsJsonArray()) {
                deleteAttributes(key, currentElement);
            }
        } else {
            if (jsonElement.isJsonObject()) {
                Set<Map.Entry<String, JsonElement>> entrySet = jsonElement.getAsJsonObject().entrySet();
                for (Map.Entry<String, JsonElement> entry : entrySet) {
                    String currentKey = entry.getKey();
                    if (currentKey.equals(key)) {
                        //Print value
                        //System.out.println(entry.getValue().toString());

                        String value = handleEspecialAttributes(key, entry);

                        if(value.equals("")) {
                            entry.setValue(JsonParser.parseString(String.valueOf(JSONObject.NULL)));
                        } else {
                            entry.setValue(JsonParser.parseString(value));
                        }

                        //Update value
                        //entry.setValue(JsonParser.parseString(String.valueOf(JSONObject.NULL)));
                        //entry.setValue(JsonParser.parseString(""));
                        //System.out.println("Key-Value Pair: " + entry.toString());
                    }
                    deleteAttributes(key, entry.getValue());
                }
            } else {
                if (jsonElement.toString().equals(key)) {
                    //System.out.println(jsonElement.toString());
                }
            }
        }
    }

    private String beautifyJSON(String data){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement jsonElement = JsonParser.parseString(data);
        data = gson.toJson(jsonElement);

        return data;
    }

    private void end(long startTime){
        long endTime = System.nanoTime();
        long totalTime_nanoseconds = endTime - startTime;
        double totalTime_seconds = (double) totalTime_nanoseconds / 1000000000;
        System.out.print("Time needed (seconds): ");
        System.out.format("%3f%n", totalTime_seconds);

    }

    private String handleEspecialAttributes(String key, Map.Entry<String, JsonElement> entry) throws JSONException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyStoreException, SignatureException, InvalidKeyException, InvalidKeySpecException {
        String value = "";

        if (key.equals("target") && profile.equals("Provenance")) {
            findResourcesWithProvenance(entry);
            value = entry.getValue().toString();
        }

        if (key.equals("meta")) {
            value = anonymizeMetadata(entry);
        } else if (key.equals("address")) {
            value = anonymizeAddress(entry);
        } else if (key.equals("reference")) {
            value = "\"" + setReferenceValue(entry) + "\"";
        } else if (key.equals("subject") || key.equals("link") || key.equals("targetReference") || key.equals("requester") || (key.equals("target") && !profile.equals("Provenance")) || key.equals("who") || key.equals("observer") || key.equals("condition") || key.equals("what") || key.equals("other") || (key.equals("encounter") && profile.equals("CarePlan")) || (key.equals("author") && profile.equals("CarePlan")) || (key.equals("author") && !profile.equals("DocumentReference")) || key.equals("patient") || key.equals("actor")) {
            value = anonymizeReferenceObject(entry);
        } else if (key.equals("identifier") || key.equals("masterIdentifier") || key.equals("targetIdentifier") || key.equals("preAdmissionIdentifier")) {
            value = anonymizeIdentifier(entry);
        } else if (key.equals("gender")) {
            value = "unknown";
        } else if((key.equals("when") && profile.equals("Signature")) || (key.equals("recorded") && profile.equals("Provenance")) || (key.equals("recorded") && profile.equals("AuditEvent")) || key.equals("timestamp") || (key.equals("lastModified") && profile.equals("BundleEntry")) || (key.equals("date") && profile.equals("DocumentReference"))) {
            value = "\"" + setRandomDateTime() + "\"";
        } else if (key.equals("birthDate") || key.equals("effectiveDateTime") || key.equals("deceasedDateTime") || key.equals("created") || key.equals("date") || key.equals("authoredOn") || key.equals("recorded") || key.equals("onsetDateTime") || key.equals("occurrenceDateTime")) {
            String dateTime = entry.getValue().toString();
            value = anonymizeDateTime(dateTime);
            value = "\"" + value + "\"";
        } else if (key.equals("name")) {
            value = anonymizeName(entry);
        } else if (key.equals("id")) {
            //value = "\"" + setRandomIdentifier() + "\"";
            value = "\"" + assignNewID(entry.getValue().toString()) + "\"";
        } else if (key.equals("fullUrl")) {
            value = "\"" + setFullUrlValue(entry) + "\"";
        } else if (key.equals("effectivePeriod") || key.equals("period") || key.equals("onsetPeriod")) {
            value = anonymizePeriod(entry);
        } else if ((key.equals("attachment") && profile.equals("DocumentReference")) || (key.equals("content") && profile.equals("Media"))) {
            value = anonymizeAttachment(entry);
        } else if (key.equals("section") && profile.equals("Composition")) {
            value = renameAttributesInComposition(entry.getValue().toString(), originalAttributes, temporaryAttributes);
        } else if (key.equals("signature") && profile.equals("Provenance")) {
            value = addSignature(entry);
        }

        return value;
    }

    private String setRandomIdentifier(){
        SecureRandom secureRandom = new SecureRandom();
        long random = secureRandom.nextLong();
        if (random < 0) {
            random = (-1) * random;
        }
        return Long.toString(random);
    }

    private boolean isBundle(String data) throws JSONException {
        String resourceType = getResourceType(data);
        if(resourceType.equals("Bundle")){
            return true;
        } else {
            return false;
        }
    }

    private String getResourceType(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        String resourceType = jsonObject.get("resourceType").toString();
        //System.out.println("resourceType: " + resourceType);
        return resourceType;
    }

    private String anonymizeBundle(String data) throws JSONException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyStoreException, SignatureException, InvalidKeyException, InvalidKeySpecException {
        JSONObject jsonObject = new JSONObject(anonymizeBundleGeneral(data));
        String anonymizedData = "";
        JSONArray entries = new JSONArray();
        if(jsonObject.has("entry")) {
            JSONArray jsonArray = jsonObject.getJSONArray("entry");
            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                JSONObject entry = jsonArray.getJSONObject(i);
                profile = "BundleEntry";
                entries.put(new JSONObject(anonymizeFHIResource(entry.toString(), "BundleEntry")));

                JSONObject resource = (JSONObject) entry.get("resource");
                String profileType = resource.get("resourceType").toString();
                currentID = ids.get(resource.get("id").toString());
                profile = profileType;

                hasProvenanceExtension = provenanceExtensionExists(resource);

                JSONObject resourceUpdated = entries.getJSONObject(i);
                resourceUpdated.put("resource", new JSONObject(anonymizeFHIResource(resource.toString(), profileType)));
                maintainAnonDataPerResource(currentID, resourceUpdated.toString());

            }
            jsonObject.put("entry", entries);
        }
        anonymizedData = jsonObject.toString();
        return anonymizedData;
    }

    private JSONArray createExtension(String mechanism) throws JSONException {
        JSONArray jsonArray = new JSONArray()
                .put(new JSONObject()
                        .put("url", "http://interopehrate.eu/fhir/StructureDefinition/AnonymizationExtension-IEHR")
                        .put("valueCoding", new JSONObject()
                                .put("system", "http://interopehrate.eu/fhir/CodeSystem/AnonymizationType-IEHR")
                                .put("code", mechanism.toLowerCase())
                                .put("display", mechanism)));

        return jsonArray;
    }

    private String replaceID(String oldValue, String firstCharacter, String secondCharacter) {
        String newValue = "";

        if(oldValue.contains("/")) {
            if (!firstCharacter.equals("") && !secondCharacter.equals("")) {
                //String firstCharacter = "\"";
                //String secondCharacter = "/";
                int start = oldValue.indexOf(firstCharacter) + 1;
                int end = oldValue.lastIndexOf(secondCharacter) + 1;
                newValue = oldValue.substring(start, end) + assignNewID(oldValue.substring(end));
            } else if(!firstCharacter.equals("")) {
                //String firstCharacter = "/";
                int start = oldValue.lastIndexOf(firstCharacter) + 1;
                newValue = oldValue.substring(start);
            } else if(!secondCharacter.equals("")){
                //String secondCharacter = "/";
                int end = oldValue.lastIndexOf(secondCharacter) + 1;
                newValue = oldValue.substring(0, end) + assignNewID(oldValue.substring(end));
            }
        } else {
            newValue = assignNewID(oldValue);
        }


        return newValue;
    }

    private String setReferenceValue(Map.Entry<String, JsonElement> entry) throws JSONException {
        String oldValue = entry.getValue().toString();
        String newValue = "";
        if(!oldValue.equals("\"\"")) {
            if(isJSON(oldValue)){
                if(entry.getValue().isJsonObject()){
                    JSONObject reference = new JSONObject(oldValue);
                    if(reference.has("reference")){
                        oldValue = reference.get("reference").toString();
                        //int end = oldValue.lastIndexOf("/") + 1;
                        //newValue = oldValue.substring(0, end) + assignNewID(oldValue.substring(end));
                        String secondCharacter = "/";
                        newValue = replaceID(oldValue, "", secondCharacter);
                    }
                }
            } else {
                //int start = oldValue.indexOf("\"") + 1;
                //int end = oldValue.lastIndexOf("/") + 1;
                //newValue = oldValue.substring(start, end) + assignNewID(oldValue.substring(end));
                String firstCharacter = "\"";
                String secondCharacter = "/";
                newValue = replaceID(oldValue, firstCharacter, secondCharacter);
            }
        }

        return newValue;
    }

    private String setFullUrlValue(Map.Entry<String, JsonElement> entry) {
        String oldValue = entry.getValue().toString();
        //int start = oldValue.indexOf("\"") + 1;
        //int end = oldValue.lastIndexOf("/") + 1;
        //String newValue = oldValue.substring(start, end) + assignNewID(oldValue.substring(end));
        String firstCharacter = "\"";
        String secondCharacter = "/";
        String newValue = replaceID(oldValue, firstCharacter, secondCharacter);

        return newValue;
    }

    private boolean isJSON(String data) {
        try {
            new JSONObject(data);
        } catch (JSONException ex) {
            try {
                new JSONArray(data);
            } catch (JSONException exc) {
                return false;
            }
        }
        return true;
    }

    private String anonymizeIdentifier(Map.Entry<String, JsonElement> entry) throws JSONException {
        JsonElement identifier = entry.getValue();
        String value = "";
        if(identifier.isJsonObject()) {
            JSONObject jsonObject = new JSONObject()
                    .put("value", currentID);
            value = jsonObject.toString();
        } else if (identifier.isJsonArray()) {
            JSONArray jsonArray = new JSONArray()
                    .put(new JSONObject()
                            .put("value", currentID));
            value = jsonArray.toString();
        }
        //System.out.println("identifier " + value);
        return value;
    }

    private String anonymizeAddress(Map.Entry<String, JsonElement> entry) throws JSONException {
        JsonElement element = entry.getValue();
        JSONArray addresses = new JSONArray();
        String value = "";
        if(element.isJsonArray()) {
            JSONArray address = new JSONArray(element.toString());
            for(int i = 0; i < address.length(); i++) {
                if(address.getJSONObject(i).has("country")) {
                    addresses.put(new JSONObject()
                            .put("country", address.getJSONObject(i).get("country").toString()));
                }
            }
            if(addresses.length() > 0) {
                value = addresses.toString();
            } else {
                JSONArray jsonArray = new JSONArray()
                        .put(new JSONObject()
                                .put("country", "UNK"));
                value = jsonArray.toString();
            }
        } else if (element.isJsonObject()) {
            JSONObject address = new JSONObject(element.toString());
            JSONObject addressUpdated = new JSONObject();
            if(address.has("country")) {

                addressUpdated.put("country", address.get("country").toString());
            } else {
                addressUpdated.put("country", "UNK");
            }
            value = addressUpdated.toString();
        }
        return value;
    }

    private String anonymizeName(Map.Entry<String, JsonElement> entry) throws JSONException {
        JsonElement name = entry.getValue();
        String value = "";
        if(isJSON(entry.getValue().toString())){
            if(name.isJsonObject()) {
                JSONObject jsonObject = new JSONObject()
                        .put("family", "Anonymous");
                value = jsonObject.toString();
            } else if (name.isJsonArray()) {
                JSONArray jsonArray = new JSONArray()
                        .put(new JSONObject()
                                .put("family", "Anonymous"));
                value = jsonArray.toString();
            }
        } else {
            value = "Anonymous";
        }
        return value;
    }

    private String anonymizeReferenceObject(Map.Entry<String, JsonElement> entry) throws JSONException {
        JSONObject jsonObject = new JSONObject()
                .put("reference", setReferenceValue(entry));
        return jsonObject.toString();
    }

    private String anonymizeDateTime(String dateTime) {
        dateTime = dateTime.replaceAll("\"", "");
        String value = "";
        boolean containsDate = Pattern.compile("^[0-9]{4}[-]").matcher(dateTime).find();
        if(containsDate) {
            String[] date = dateTime.split("-", 2);
            String year = date[0];
            value = year;
        } else {
            boolean containsYear = Pattern.compile("^[0-9]{4}$").matcher(dateTime).find();
            if(containsYear) {
                value = dateTime;
            }
        }
        return value;
    }

    private String anonymizeMetadata(Map.Entry<String, JsonElement> entry) throws JSONException {
        JSONObject meta = new JSONObject(entry.getValue().toString());
        String value = "";
        if(meta.has("profile")) {
            JSONObject metaUpdated = new JSONObject();
            metaUpdated.put("profile", meta.get("profile"));
            value = metaUpdated.toString();
        }
        return value;
    }

    private String addExtension(String anonymizedData) throws JSONException {
        String[] key = {"id", "meta"};
        String extension = "\"addNewExtensionHere\":\"tempValue\"";
        JSONObject jsonObject = new JSONObject(anonymizedData);
        String dataWithExtension = "";
        boolean flag = false;
        int start;
        int end;
        for(int i = key.length - 1; i >= 0; i--) {
            if(jsonObject.has(key[i])) {
                String value = jsonObject.get(key[i]).toString();
                int length = value.length();
                String data = jsonObject.toString();
                int position = data.indexOf(value) + length;
                if(isJSON(value)) {
                    start = position;
                    end = position + 1;
                } else {
                    start = position + 1;
                    end = position + 2;
                }
                dataWithExtension = data.substring(0, end) + extension + data.substring(start);
                flag = true;
                break;
            }
        }

        if(flag) {
            JSONObject appendExtension = new JSONObject(dataWithExtension);
            appendExtension.put("addNewExtensionHere", createExtension(mechanism));
            return appendExtension.toString();
        } else {
            jsonObject.put("addNewExtensionHere", createExtension(mechanism));
            return jsonObject.toString();
        }
    }

    private String anonymizePeriod(Map.Entry<String, JsonElement> entry) throws JSONException {
        JsonElement element = entry.getValue();
        JSONArray periods = new JSONArray();
        String value = "";
        if(isJSON(element.toString())){
            if(element.isJsonArray()) {
                JSONArray period = new JSONArray(element.toString());
                for(int i = 0; i < period.length(); i++) {
                    if(period.getJSONObject(i).has("start")) {
                        periods.put(new JSONObject()
                                .put("start", anonymizeDateTime(period.getJSONObject(i).get("start").toString())));
                    }
                }
                value = periods.toString();
            } else if (element.isJsonObject()) {
                JSONObject period = new JSONObject(element.toString());
                JSONObject periodUpdated = new JSONObject();
                if(period.has("start")) {
                    periodUpdated.put("start", anonymizeDateTime(period.get("start").toString()));
                }
                value = periodUpdated.toString();
            }
        } else {
            String dateTime = anonymizeDateTime(element.toString());
            value = dateTime;
        }

        return value;
    }

    private String anonymizeAttachment(Map.Entry<String, JsonElement> entry) throws JSONException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyStoreException, SignatureException, InvalidKeyException, InvalidKeySpecException {
        String attachment = entry.getValue().toString();

        return anonymizeFHIResource(attachment, "Attachment");
    }

    private String setRandomDateTime() {
        int year = setRandomYear();
        int month = setRandomMonth();
        int day = setRandomDay(month);

        String date = year + "-" + valueToString(month) + "-" + valueToString(day) + "T00:00:00Z";

        return date;
    }

    private int setRandomYear() {
        Calendar calendar  = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -15);

        int minYear = calendar.get(Calendar.YEAR);
        int maxYear = minYear + 15;

        SecureRandom secureRandom = new SecureRandom();

        return secureRandom.nextInt((maxYear - minYear) + 1) + minYear;
    }

    private int setRandomMonth() {
        SecureRandom secureRandom = new SecureRandom();
        return 1 + secureRandom.nextInt(12);
    }

    private int setRandomDay(int month) {
        SecureRandom secureRandom = new SecureRandom();
        int upperLimit = 0;
        int value = 0;

        if(month % 2 == 0) {
            //System.out.println(month + " is even.");
            if(month <= 6) {
                upperLimit = 30;
            } else {
                upperLimit = 31;
            }

        } else {
            //System.out.println(month + " is odd.");
            if(month <= 7) {
                upperLimit = 31;
            } else {
                upperLimit = 30;
            }
        }

        value = 1 + secureRandom.nextInt(upperLimit);

        return value;
    }

    private String valueToString(int value) {
        if(value < 10) {
            return "0" + value;
        } else {
            return Integer.toString(value);
        }
    }

    private String renameAttributesInComposition(String data, ArrayList<String> oldValue, ArrayList<String> newValue) {
        for(int i = 0; i < oldValue.size(); i++) {
            data =  data.replaceAll("\""+oldValue.get(i)+"\":", "\""+newValue.get(i)+"\":");
        }

        return data;
    }

    private String addSignature(Map.Entry<String, JsonElement> entry) throws JSONException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyStoreException, SignatureException, InvalidKeyException, InvalidKeySpecException {
        JSONArray signatures = new JSONArray(entry.getValue().toString());
        JSONArray jsonArray = new JSONArray();
        for(int i = 0; i < signatures.length(); i++) {
            JSONObject signature = signatures.getJSONObject(i);
            JSONObject jsonObject = new JSONObject()
                    //.put("id", "Signature/" + setRandomIdentifier())
                    .put("id", "Signature/" + assignNewID(signature.get("id").toString()))
                    .put("type",  signature.get("type"))
                    .put("when", setRandomDateTime())
                    .put("who", signature.get("who"))
                    .put("targetFormat", signature.get("targetFormat"))
                    .put("sigFormat", signature.get("sigFormat"))
                    .put("data", createSignature());
            jsonArray.put(jsonObject);
        }

        //System.out.println("Signature: " + jsonArray.toString());
        return jsonArray.toString();

    }

    private void findResourcesWithProvenance(Map.Entry<String, JsonElement> entry) throws JSONException {
        JSONArray target = new JSONArray(entry.getValue().toString());
        String json = "";
        for(int i = 0; i < target.length(); i++) {
            String reference = target.getJSONObject(i).get("reference").toString();
            //int start = reference.lastIndexOf("/") + 1;
            //String profileID = reference.substring(start);
            String firstCharacter = "/";
            String profileID = replaceID(reference, firstCharacter, "");
            System.out.println(reference);
            if (anonDataPerResource.containsKey(profileID)) {
                json = anonDataPerResource.get(profileID);
                jsonValue = beautifyJSON(json);
                //System.out.println("JSON to be signed: " + jsonValue);
            } else if (ids.containsKey(profileID)) {
                String id = ids.get(profileID);
                json = anonDataPerResource.get(id);
                jsonValue = beautifyJSON(json);
                //System.out.println("JSON to be signed: " + jsonValue);
            } else {
                jsonValue = "Resource " + reference + " could not be found.";
            }
        }
    }

    private void maintainAnonDataPerResource(String id, String resource) {
        if(!anonDataPerResource.containsKey(id)){
            anonDataPerResource.put(id, resource);
        }
    }

    private String createSignature() throws CertificateException, SignatureException, NoSuchAlgorithmException, InvalidKeyException, IOException, InvalidKeySpecException, UnrecoverableKeyException, KeyStoreException {
        String resource = jsonValue;
        CryptoManagement cryptoManagement = CryptoManagementFactory.create(CA_URL);

        PrivateKey privateKey = cryptoManagement.getPrivateKey(getContext());
        String signed = cryptoManagement.signPayload(resource, privateKey);
        byte[] certificateData = cryptoManagement.getCertificateFromKeystore(getContext());

        String jwsToken = cryptoManagement.createDetachedJws(certificateData, signed);
        Boolean verifyJws = cryptoManagement.verifyDetachedJws(jwsToken, resource);

        //String jwsToken = "$signature$";
        //System.out.println("Signature:" + jwsToken);
        return jwsToken;
    }

    private boolean provenanceExtensionExists(JSONObject resource) throws JSONException {
        hasProvenanceExtension = false;

        if (resource.has("extension")) {
            JSONArray extension = (JSONArray) resource.get("extension");
            for(int i = 0; i < extension.length(); i++) {
                if(extension.getJSONObject(i).has("url")) {
                    if(extension.getJSONObject(i).get("url").toString().equals("http://interopehrate.eu/fhir/StructureDefinition/ProvenanceExtension-IEHR")) {
                        handleProvenanceID(extension.getJSONObject(i).toString());
                        hasProvenanceExtension = true;
                        break;
                    }
                }
            }
        }

        return hasProvenanceExtension;
    }

    private String appendProvenanceExtension(String anonymizedResource) throws JSONException {
        JSONObject resource = new JSONObject(anonymizedResource);

        if (resource.has("addNewExtensionHere")) {
            JSONArray addNewExtensionHere = (JSONArray) resource.get("addNewExtensionHere");
            addNewExtensionHere.put(createProvenanceExtension());

        }
        return resource.toString();
    }

    private JSONObject createProvenanceExtension() throws JSONException {
        JSONObject provenanceExtension = new JSONObject()
                .put("url", "http://interopehrate.eu/fhir/StructureDefinition/ProvenanceExtension-IEHR")
                .put("valueReference", new JSONObject()
                        //.put("reference", "Provenance/" + setRandomIdentifier()));
                        .put("reference", "Provenance/" + ids.get(provenanceExtensionID)));
        return provenanceExtension;
    }

    private String anonymizeBundleGeneral(String data) throws JSONException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyStoreException, SignatureException, InvalidKeyException, InvalidKeySpecException {
        profile = "BundleGeneral";
        JSONObject jsonObject = new JSONObject(data);
        JSONArray entryValues = new JSONArray();
        if(jsonObject.has("entry")) {
            entryValues = (JSONArray) jsonObject.get("entry");
            jsonObject.remove("entry");
            data = jsonObject.toString();
        }

        JsonObject bundleGeneral = JsonParser.parseString(data).getAsJsonObject();
        if(Attributes.attributesPerProfile.containsKey("BundleGeneral")) {
            for (String currentKey : Attributes.attributesPerProfile.get("BundleGeneral")) {
                deleteAttributes(currentKey, bundleGeneral);
            }
        }

        String anonymizedData = bundleGeneral.toString();
        JSONObject bundle = new JSONObject(anonymizedData);
        bundle.put("entry", entryValues);
        return bundle.toString();
    }

    private void findElementsChildren(JsonElement jsonElement, String key) {
        if(jsonElement.isJsonObject()) {
            Set<Map.Entry<String, JsonElement>> entrySet = jsonElement.getAsJsonObject().entrySet();
            for (Map.Entry<String, JsonElement> entry : entrySet) {
                String currentKey = entry.getKey();
                if (currentKey.equals(key)) {
                    createNewID(entry.getValue().toString());
                }
                findElementsChildren(entry.getValue(), key);
            }
        } else if(jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (JsonElement childElement : jsonArray) {
                findElementsChildren(childElement, key);
            }
        }
    }

    private void createNewID(String oldID) {
        oldID = keepOnlyID(oldID);
        String newID = "";
        if(!ids.containsKey(oldID)){
            if(mechanism.equals("Pseudonymization") && ids.size() == 0) {
                newID = randomValue;
            } else {
                newID =  setRandomIdentifier().toString();
            }
            ids.put(oldID, newID);
        }
    }

    private String assignNewID(String oldID) {
        oldID = keepOnlyID(oldID);
        String newID = "";
        if(ids.containsKey(oldID)){
            newID = ids.get(oldID);
        } else {
            if(checkID(oldID)) {
                createNewID(oldID);
                newID = ids.get(oldID);
            } else {
                newID = oldID;
            }
        }
        return newID;
    }

    private void handleProvenanceID(String extension) throws JSONException {
        String oldID = "";
        JSONObject provenanceExtension = new JSONObject(extension);
        if(provenanceExtension.has("valueReference")) {
            JSONObject valueReference = (JSONObject) provenanceExtension.get("valueReference");
            if(valueReference.has("reference")) {
                oldID = valueReference.get("reference").toString();
                provenanceExtensionID = keepOnlyID(oldID);
                createNewID(oldID);
            }
        }
    }

    private boolean checkID(String value) {
        if(!ids.containsValue(value)) {
            return true;
        } else {
            return false;
        }
    }

    private String keepOnlyID(String oldID) {
        if(oldID.contains("/")) {
            int start = oldID.indexOf("/") + 1;
            oldID = oldID.substring(start);
        }
        return oldID.replace("\"", "");
    }

}



