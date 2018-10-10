
Alfresco Ciphering
================================================

This add-on replaces abandoned *addons* to cipher and decipher files with a password in Alfresco:

* [alfcrypto](https://github.com/fegorama/alfcrypto)
* [alfresco-crypt](https://sourceforge.net/projects/alfresco-crypt/) 

The addon creates a new `PKCS5` mime type is added to identify ciphered files. 

**License**
The plugin is licensed under the [LGPL v3.0](http://www.gnu.org/licenses/lgpl-3.0.html). 

**State**
Current addon release is 0.8.0

**Compatibility**
The current version has been developed using Alfresco 201707 and Alfresco SDK 3.0.1

***No original Alfresco resources have been overwritten***

Downloading the ready-to-deploy-plugin
--------------------------------------
The binary distribution is made of one JAR file to be deployed in Alfresco as a repo module:

* [repo JAR](https://github.com/keensoft/alfresco-ciphering/releases/download/0.8.0/alfresco-ciphering-repo-0.8.0)

You can install it by copying JAR file to `$ALFRESCO_HOME/modules/platform` and re-starting Alfresco.

There is also one JAR file for Share Web App:

* [share JAR](https://github.com/keensoft/alfresco-ciphering/releases/download/0.8.0/alfresco-ciphering-share-0.8.0)

You can install it by copying JAR file to `$ALFRESCO_HOME/modules/share` and re-starting Alfresco.


Building the artifacts
----------------------
You can build the artifacts from source code using maven in `alfresco-ciphering-repo` and `alfresco-ciphering-share` folders.

```
$ mvn clean package
```

Configuration
-------------
Job can be planified by including following properties in `alfresco-global.properties`
```
cipher.secret.key.factory
cipher.secret.key.spec
cipher.instance
```
By default, following values are pre-configured

```
cipher.secret.key.factory=PBKDF2WithHmacSHA256
cipher.secret.key.spec=AES
cipher.instance=AES/CBC/PKCS5Padding
```

Using
-----

* Configure a rule in a folder to apply `keensoft-cipher-action` for every incoming file. Include also a *password* in the box to the right.

* Once a file is ciphered in PKCS5 format, a new action `Decipher` will be added to Share Web App. *Password* is required in order to decipher the file.

Contributors
------------
* [fsckawk](https://github.com/fsckawk)
