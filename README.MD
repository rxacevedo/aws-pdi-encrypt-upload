# AWS PDI Encrypt

A tiny app that can:
- Take a file and zip it (GZIP)
- Securely/unsececurely upload to S3


## Setup

Create the following files and specify the values as seen below:

**aws.profile**  
[AWSProfile]  
aws_access_key_id=  
aws_secret_access_key=  

**aws.properties**  
s3bucket=  
keyDir=  
dataDir=  



## Requirements

### JCE
Need to have Java Cryptography Extension (JCE) Unlimited installed.

**Instructions:**  
1. Download and install "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy File" from Oracle matching your JRE  
2. Unzip the downloaded file  
3. Copy local_policy.jar and US_export_policy.jar to $JAVA_HOME/jre/lib/security (replace existing)  
4. Restart  


### Resources
http://java.awsblog.com/post/Tx3305Q4J6AVNQK/Amazon-S3-Client-Side-Authenticated-Encryption
