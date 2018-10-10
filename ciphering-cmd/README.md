Alfresco Ciphering - Command Line Tool
================================================

## Installation

Download jar file from [ciphering-cmd-1.0.0.jar](https://github.com/keensoft/alfresco-ciphering/releases/download/1.0.0/ciphering-cmd-1.0.0.jar)

## Requirements

Java 8 is required.

## Usage

Deciphering arguments:

```
usage: java -jar ciphering-cmd-1.0.0.jar --input=c:/temp/file.pkcs5 --output=c:/temp/file.ext [options]

  --input  FILE  Set encrypted input file.
  --output FILE  Set decrypted output file.
  --secret.key.factory VALUE Factory algorithm, PBKDF2WithHmacSHA256 by default
  --secret.key.spec    VALUE Ciphering algorithm, AES by default
  --cipher.instance    VALUE Cipher Instance type, AES/CBC/PKCS5Padding by default

```

* Files are expressed in absolute paths
* Deciphering program will prompt you for the password
