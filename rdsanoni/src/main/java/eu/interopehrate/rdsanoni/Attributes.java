package eu.interopehrate.rdsanoni;

import java.util.ArrayList;
import java.util.HashMap;

public class Attributes {

    public static final ArrayList<String> diagnosticReport = new ArrayList<String>() {{
        add("id");
        add("meta");
        add("text");
        add("contained");
        add("extension");
        add("modifierExtension");
        add("identifier");
        add("basedOn");
        add("subject");
        add("reference");
        add("type");
        add("encounter");
        add("effectiveDateTime");
        add("effectivePeriod");
        add("issued");
        add("result");
        add("comment");
        add("link");
        add("presentedForm");

    }};

    public static final ArrayList<String> patient = new ArrayList<String>() {{
        add("id");
        add("meta");
        add("text");
        add("contained");
        add("extension");
        add("modifierExtension");
        add("identifier");
        add("name");
        add("given");
        add("prefix");
        add("suffix");
        add("period");
        add("telecom");
        //add("gender");
        add("birthDate");
        add("deceasedDateTime");
        add("address");
        add("line");
        add("city");
        add("district");
        add("state");
        add("postalCode");
        add("maritalStatus");
        add("multipleBirthBoolean");
        add("multipleBirthInteger");
        add("photo");
        add("contact");
        add("relationship");
        add("organization");
        add("generalPractitioner");
        add("managingOrganization");
        add("link");
        add("other");

    }};

    public static final ArrayList<String> documentManifest = new ArrayList<String>() {{
        add("id");
        add("meta");
        add("text");
        add("contained");
        add("extension");
        add("modifierExtension");
        add("masterIdentifier");
        add("identifier");
        add("subject");
        add("reference");
        add("created");
        add("recipient");
        add("source");
        add("description");
        add("related");
        add("ref");

    }};

    public static final ArrayList<String> attachment = new ArrayList<String>() {{
        add("id");
        add("extension");
        add("url");
        add("creation");

    }};

    public static final ArrayList<String> composition = new ArrayList<String>() {{
        add("section");
        add("id");
        add("meta");
        add("text");
        add("contained");
        add("extension");
        add("modifierExtension");
        add("identifier");
        add("subject");
        add("encounter");
        add("date");
        add("reference");
        add("attester");
        add("time");
        add("party");
        add("custodian");
        add("relatesTo");
        add("targetIdentifier");
        add("targetReference");
        add("event");
        add("detail");
        add("period");
        add("focus");
        add("entry");

    }};

    public static final ArrayList<String> medicationRequest = new ArrayList<String>() {{
        add("id");
        add("meta");
        add("text");
        add("contained");
        add("extension");
        add("modifierExtension");
        add("identifier");
        add("reportedReference");
        add("subject");
        add("reference");
        add("encounter");
        add("supportingInformation");
        add("authoredOn");
        add("requester");
        add("performer");
        add("recorder");
        add("reasonReference");
        add("instantiatesCanonical");
        add("instantiatesUri");
        add("basedOn");
        add("groupIdentifier");
        add("insurance");
        add("note");
        add("dispenseRequest");
        add("validityPeriod");
        add("priorPrescription");
        add("detectedIssue");
        add("eventHistory");

    }};

    public static final ArrayList<String> documentReference = new ArrayList<String>() {{
        add("id");
        add("meta");
        add("text");
        add("contained");
        add("extension");
        add("modifierExtension");
        add("masterIdentifier");
        add("identifier");
        add("subject");
        add("reference");
        add("date");
        add("author");
        add("authenticator");
        add("custodian");
        add("relatesTo");
        add("target");
        add("description");
        add("attachment");
        add("encounter");
        add("period");
        add("sourcePatientInfo");
        add("related");

    }};

    public static final ArrayList<String> auditEvent = new ArrayList<String>() {{
        add("id");
        add("meta");
        add("text");
        add("contained");
        add("extension");
        add("modifierExtension");
        add("period");
        add("recorded");
        add("outcomeDesc");
        add("role");
        add("who");
        add("reference");
        add("altId");
        add("name");
        add("location");
        add("policy");
        add("network");
        add("address");
        add("site");
        add("observer");
        add("entity");
        add("what");
        add("securityLabel");
        add("description");
        add("detail");

    }};

    public static final ArrayList<String> bundleGeneral = new ArrayList<String>() {{
        add("id");
        add("meta");
        add("identifier");
        add("timestamp");
        add("link");
        add("extension");
        add("modifierExtension");
        add("relation");
        add("url");
        add("signature");

    }};

    public static final ArrayList<String> bundleEntry = new ArrayList<String>() {{
        add("fullUrl");
        add("search");
        add("request");
        add("response");
        add("location");
        add("etag");
        add("lastModified");
        add("outcome");

    }};

    public static final ArrayList<String> media = new ArrayList<String>() {{
        add("id");
        add("meta");
        add("text");
        add("contained");
        add("extension");
        add("modifierExtension");
        add("identifier");
        add("basedOn");
        add("partOf");
        add("modality");
        add("subject");
        add("reference");
        add("encounter");
        add("createdDateTime");
        add("createdPeriod");
        add("issued");
        add("operator");
        add("reasonCode");
        add("deviceName");
        add("device");
        add("content");
        add("note");

    }};

    public static final ArrayList<String> practitioner = new ArrayList<String>() {{
        add("id");
        add("meta");
        add("text");
        add("contained");
        add("extension");
        add("modifierExtension");
        add("identifier");
        add("name");
        add("given");
        add("prefix");
        add("suffix");
        add("period");
        add("telecom");
        //add("gender");
        add("birthDate");
        add("address");
        add("photo");
        add("qualification");
        add("issuer");

    }};

    public static final ArrayList<String> organization = new ArrayList<String>() {{
        add("id");
        add("meta");
        add("text");
        add("contained");
        add("extension");
        add("modifierExtension");
        add("identifier");
        add("name");
        add("alias");
        add("telecom");
        add("address");
        add("partOf");
        add("contact");
        add("endpoint");

    }};

    public static final ArrayList<String> encounter = new ArrayList<String>() {{
        add("id");
        add("meta");
        add("text");
        add("contained");
        add("extension");
        add("modifierExtension");
        add("identifier");
        add("period");
        add("subject");
        add("reference");
        add("episodeOfCare");
        add("basedOn");
        add("participant");
        add("individual");
        add("appointment");
        add("reasonReference");
        add("condition");
        add("account");
        add("preAdmissionIdentifier");
        add("origin");
        add("specialCourtesy");
        add("destination");
        add("serviceProvider");
        add("partOf");

    }};

    public static final ArrayList<String> observation = new ArrayList<String>() {{
        add("id");
        add("meta");
        add("text");
        add("contained");
        add("extension");
        add("modifierExtension");
        add("identifier");
        add("basedOn");
        add("partOf");
        add("display");
        add("subject");
        add("reference");
        add("focus");
        add("encounter");
        add("effectiveDateTime");
        add("effectivePeriod");
        add("issued");
        add("note");
        add("specimen");
        add("device");
        add("hasMember");
        add("derivedFrom");
        add("valueString");
        add("valueDateTime");
        add("valuePeriod");

    }};

    public static final ArrayList<String> condition = new ArrayList<String>() {{
        add("id");
        add("meta");
        add("text");
        add("contained");
        add("extension");
        add("modifierExtension");
        add("identifier");
        add("subject");
        add("reference");
        add("encounter");
        add("onsetDateTime");
        add("onsetAge");
        add("onsetPeriod");
        add("onsetString");
        add("abatementDateTime");
        add("abatementAge");
        add("abatementPeriod");
        add("abatementString");
        add("recordedDate");
        add("recorder");
        add("asserter");
        add("assessment");
        add("detail");
        add("note");
        add("authorReference");
        add("authorString");
        add("time");

    }};

    public static final ArrayList<String> carePlan = new ArrayList<String>() {{
        add("id");
        add("meta");
        add("text");
        add("contained");
        add("extension");
        add("modifierExtension");
        add("identifier");
        add("instantiatesCanonical");
        add("instantiatesUri");
        add("basedOn");
        add("replaces");
        add("partOf");
        add("subject");
        add("reference");
        add("encounter");
        add("period");
        add("created");
        add("author");
        add("contributor");
        add("careTeam");
        add("addresses");
        add("supportingInfo");
        add("goal");
        add("outcomeReference");
        add("progress");
        add("reasonReference");
        add("scheduledTiming");
        add("scheduledPeriod");
        add("scheduledString");
        add("location");
        add("performer");
        add("productReference");
        add("note");

    }};

    public static final ArrayList<String> provenance = new ArrayList<String>() {{
        add("id");
        add("meta");
        add("text");
        add("contained");
        add("extension");
        add("modifierExtension");
        add("target");
        add("reference");
        add("occurredPeriod");
        add("occurredDateTime");
        add("recorded");
        add("policy");
        add("location");
        add("who");
        add("onBehalfOf");
        add("what");
        add("signature");

    }};

    public static final ArrayList<String> immunization = new ArrayList<String>() {{
        add("id");
        add("meta");
        add("text");
        add("contained");
        add("extension");
        add("modifierExtension");
        add("identifier");
        add("patient");
        add("reference");
        add("display");
        add("encounter");
        add("occurrenceDateTime");
        add("occurrenceString");
        add("recorded");
        add("location");
        add("actor");
        add("note");
        add("reasonReference");
        add("documentType");
        add("presentationDate");
        add("date");
        add("detail");
        add("authority");

    }};

    public static final ArrayList<String> medicationStatement = new ArrayList<String>() {{
        add("id");
        add("meta");
        add("text");
        add("contained");
        add("extension");
        add("modifierExtension");
        add("identifier");
        add("basedOn");
        add("partOf");
        add("subject");
        add("reference");
        add("type");
        add("display");
        add("context");
        add("effectiveDateTime");
        add("effectivePeriod");
        add("dateAsserted");
        add("informationSource");
        add("derivedFrom");
        add("reasonReference");
        add("note");

    }};

    public static final ArrayList<String> medication = new ArrayList<String>() {{
        add("id");
        add("meta");
        add("text");
        add("contained");
        add("extension");
        add("modifierExtension");
        add("identifier");
        add("manufacturer");
        add("reference");

    }};

    public static final ArrayList<String> other = new ArrayList<String>() {{
        add("id");
        add("meta");
        add("text");
        add("contained");
        add("extension");
        add("modifierExtension");
        add("identifier");
        add("reference");
        add("name");
        //add("gender");
        add("address");

    }};

    public static final ArrayList<String> allergyIntolerance = new ArrayList<String>() {{
        add("id");
        add("meta");
        add("text");
        add("contained");
        add("extension");
        add("modifierExtension");
        add("identifier");
        add("patient");
        add("reference");
        add("encounter");
        add("onsetDateTime");
        add("recordedDate");
        add("recorder");
        add("asserter");
        add("lastOccurrence");
        add("note");
        add("description");
        add("onset");

    }};


    public static final HashMap<String, ArrayList<String>> attributesPerProfile = new HashMap<String, ArrayList<String>>() {{
        put("DiagnosticReport", diagnosticReport);
        put("Patient", patient);
        put("DocumentManifest", documentManifest);
        put("Attachment", attachment);
        put("Composition", composition);
        put("MedicationRequest", medicationRequest);
        put("DocumentReference", documentReference);
        put("AuditEvent", auditEvent);
        put("BundleGeneral", bundleGeneral);
        put("BundleEntry", bundleEntry);
        put("Media", media);
        put("Practitioner", practitioner);
        put("Organization", organization);
        put("Encounter", encounter);
        put("Observation", observation);
        put("Condition", condition);
        put("CarePlan", carePlan);
        put("Provenance", provenance);
        put("Immunization", immunization);
        put("MedicationStatement", medicationStatement);
        put("Medication", medication);
        put("Other", other);
        put("AllergyIntolerance", allergyIntolerance);
    }};

}