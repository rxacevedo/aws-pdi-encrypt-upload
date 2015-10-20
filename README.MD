# AWS PDI Encrypt

A tiny app that can:
- Take a file or directory path
- Zip the file to gzip
- Securely/unsececurely upload to S3


## Setup

Create the following files and specify the values as seen below:

**aws.profile** 
```
[AWSProfile]  
aws_access_key_id=  
aws_secret_access_key=  
```

**aws.properties**  
```
# Base64 encoded AES 256 bit symmetric master key.    
# Leave blank to generate one automatically   
awsMasterSymmetricKey=   
awsAlgorithm=AES   
awsAlgorithmKeyLength=256   

# S3 properties   
# Check yours @ http://docs.aws.amazon.com/general/latest/gr/rande.html#s3_region      
s3endpoint=   
s3region=   
# Bucket to upload data.   
s3bucket=   
# S3 prefix for batch load   
s3prefix=   
# Amazon profile path   
awsProfilePath=   
# Amazon profile name (Default AWSProfile)   
awsProfileName=   

# Request retry count   
awsRetryCount=10   

# Directory to store generated key file.      
# If populated it's assumed the master symmetric keys should be saved to specified location.      
# If blank the keys will be presented via stdout.      
awsLocalKeyDir=   
# Directory to store encrypted data for upload (can be same as srcDir)   
awsLocalDataDir=   

# default true   
awsSendEncrypted=true   
# default true   
awsEnableZip=false     
```

## Run
Need to pass the following arguments:
- fileOrDirectory
- propertiesPath



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
