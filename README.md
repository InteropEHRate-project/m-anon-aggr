# InteropEHRate Anonymization and Aggregation Mobile Library

## Description

The Data Anonymization and Data Pseudonymization Library has been implemented in Java programming language and can be utilized by any Android application. The aim of the library is to either anonymize or pseudonymize the personal data *– and more specifically the health data –* of the data subjects whenever they want to participate in a research study.

## Installation Guide

In order to integrate the Data Anonymization and Data Pseudonymization Library there are some steps which should be followed.

1.	In case of a Gradle project, the following line should be appended in the dependencies section of the **build.gradle** file:
```
implementation(group:'eu.interopehrate', name:'rdsanoni', version: '0.1.1')
```

2. In case of a Maven project, the same dependency should be expressed with the following Maven syntax:
 ```
<dependency>
    <groupId>eu.interopehrate</groupId>
    <artifactId>rdsanoni</artifactId>
    <version>0.1.1</version>
</dependency>
 ```
 
## Methods

The Data Anonymization and Data Pseudonymization Library is deployed at the citizens mobile phone *– by the S-EHR application –* and has five methods.

#### **setPseudo**

It stores the below variables locally on the citizen’s phone.

 <ins>Parameters:</ins>
  * the _**pseudo**_, which is either a pseudo-identity or a pseudonym,
  * the _**pseudoType**_, which indicates whether the pseudo is a pseudo-identity or a pseudonym, and
  * the _**studyID**_, which is the ID of the current research study.
    
 <ins>Response:</ins> void

#### **getPseudo**

It retrieves the previously stored pseudo (pseudo-identity or pseudonym).

 <ins>Parameters:</ins>
  * the _**studyID**_, which is the ID of the current research study.
    
 <ins>Response:</ins> The pseudo-identity or the pseudonym.
 
#### **retrievePseudonym**

It retrieves a pseudonym from the Pseudonym Provider.

 <ins>Parameters:</ins>
  * the _**anAssertion**_, which is the anonymous assertion token (*), and
  * the _**publicKey**_, which is the public key of the user's certificate.
 
 (*) This is the transient anonymous identity of the user retrieved by eIDAS at an earlier stage.

 <ins>Response:</ins> A pseudonym.
 
#### **pseudonymizeData**

It pseudonymizes the citizen’s dataset.

 <ins>Parameters:</ins>
  * the _**data**_, which will get pseudonymized,
  * the _**fileType**_, which is the type of the data file, and
  * the _**studyID**_, which is the ID of the current research study (*).
  
 (*) The studyID variable is utilized in order for the library to call getPseudo and retrieve the pseudo-identity/pseudonym from the citizen’s mobile phone.
 
 <ins>Response:</ins> The pseudonymized dataset.

#### **anonymizeData**

It anonymizes the citizen’s dataset.

 <ins>Parameters:</ins>
  * the _**data**_, which will get anonymized, and
  * the _**fileType**_, which is the type of the data file.

 <ins>Response:</ins> The anonymized dataset.
