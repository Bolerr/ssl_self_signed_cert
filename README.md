# SSL Self Signed Cert Example Project

This was an example project to for a blog post to show how to get SSL working for a Spring Boot REST API application. 

I created this project to help remind myself and hopefully spare others the frustration of an issue that we ran into when setting up SSL on a project.

We developed a script that would generate a self signed p12 keystore for the application to use for local development and to test out our process before requesting the private files / certs we needed from the client to secure the application we were developing. This script: `generate_certificates.sh` an be found under the `/certs` directory in this project.

Once we had created the keystore and verified the certs looked correct, we started up the application with the new keystore referenced in the application configuration.

```
server:
  port: 8443
  ssl:
    enabled: true
    key-store: ./certs/generated/localhost.p12
    key-store-password: keyStorePassword
    key-store-provider: SunJSSE
    key-store-type: PKCS12
    key-password: keyPassword
    key-alias: localhost
```

To our surprise we were greeted with the following error.

```
Caused by: javax.crypto.BadPaddingException: Given final block not properly padded. Such issues can arise if a bad key is used during decryption.
        at com.sun.crypto.provider.CipherCore.doFinal(CipherCore.java:991) ~[sunjce_provider.jar:1.8.0_171]
        at com.sun.crypto.provider.CipherCore.doFinal(CipherCore.java:847) ~[sunjce_provider.jar:1.8.0_171]
        at com.sun.crypto.provider.PKCS12PBECipherCore.implDoFinal(PKCS12PBECipherCore.java:399) ~[sunjce_provider.jar:1.8.0_171]
        at com.sun.crypto.provider.PKCS12PBECipherCore$PBEWithSHA1AndDESede.engineDoFinal(PKCS12PBECipherCore.java:431) ~[sunjce_provider.jar:1.8.0_171]
        at javax.crypto.Cipher.doFinal(Cipher.java:2164) ~[na:1.8.0_171]
        at sun.security.pkcs12.PKCS12KeyStore.engineGetKey(PKCS12KeyStore.java:371) ~[na:1.8.0_171]
        ... 34 common frames omitted
```

Ok, we must have done something wrong when generating the key, certificates, or keystore, right. After following a number of guides trying to recreate the keystore using different methods and a lot of googling what could be causing this error, we changed the key store password and key password to match on a whim.
```
server:
  port: 8443
  ssl:
    enabled: true
    key-store: ./certs/generated/localhost.p12
    key-store-password: keyStorePassword
    key-store-provider: SunJSSE
    key-store-type: PKCS12
    key-password: keyStorePassword
    key-alias: localhost
```

It turns out that the `passout` variable of the `OPENSSL pkcs12` command changes both the key store password and the key password to that value.

```
OPENSSL pkcs12 -export -name localhost -caname "Localhost CA" \
                -inkey ./generated/localhost.key.pem -passin env:KEY_PASSWORD \
                -in ./generated/localhost.cert.pem \
                -certfile ./generated/ca.cert.pem \
                -out ./generated/localhost.p12 -passout env:KEY_STORE_PASSWORD
```

If you would like to see this for yourself download the code and run the following commands: 

(You will need Docker and OpenSSL installed)

1. Generate self signed cert:

```
cd certs && ./generate_certificates.sh 
```

2. Build the application and docker container

```
./gradlew clean build docker
```

3. Run the docker container with the two passwords specified in `generate_certificates.sh`

```
docker run -i -p 8443:8443 -p 389:389 \
    -e 'SPRING_PROFILES_ACTIVE=Local,Container' \
    -e 'SSL_PORT=8443' \
    -e 'KEY_STORE_PASSWORD=keyStorePassword' \
    -e 'KEY_PASSWORD=keyPassword' \
    --name example-ssl -t org.example.ssl/example-ssl
```

You will see the following error:

```
org.apache.catalina.LifecycleException: Failed to start component [Connector[HTTP/1.1-8443]]
        at org.apache.catalina.util.LifecycleBase.start(LifecycleBase.java:167) ~[tomcat-embed-core-8.5.27.jar!/:8.5.27]
        at org.apache.catalina.core.StandardService.addConnector(StandardService.java:225) ~[tomcat-embed-core-8.5.27.jar!/:8.5.27]
        at org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer.addPreviouslyRemovedConnectors(TomcatEmbeddedServletContainer.java:250) [spring-boot-1.5.10.RELEASE.jar!/:1.5.10.RELEASE]
        at org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer.start(TomcatEmbeddedServletContainer.java:193) [spring-boot-1.5.10.RELEASE.jar!/:1.5.10.RELEASE]
        at org.springframework.boot.context.embedded.EmbeddedWebApplicationContext.startEmbeddedServletContainer(EmbeddedWebApplicationContext.java:297) [spring-boot-1.5.10.RELEASE.jar!/:1.5.10.RELEASE]
        at org.springframework.boot.context.embedded.EmbeddedWebApplicationContext.finishRefresh(EmbeddedWebApplicationContext.java:145) [spring-boot-1.5.10.RELEASE.jar!/:1.5.10.RELEASE]
        at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:546) [spring-context-4.3.14.RELEASE.jar!/:4.3.14.RELEASE]
        at org.springframework.boot.context.embedded.EmbeddedWebApplicationContext.refresh(EmbeddedWebApplicationContext.java:122) [spring-boot-1.5.10.RELEASE.jar!/:1.5.10.RELEASE]
        at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:693) [spring-boot-1.5.10.RELEASE.jar!/:1.5.10.RELEASE]
        at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:360) [spring-boot-1.5.10.RELEASE.jar!/:1.5.10.RELEASE]
        at org.springframework.boot.SpringApplication.run(SpringApplication.java:303) [spring-boot-1.5.10.RELEASE.jar!/:1.5.10.RELEASE]
        at org.springframework.boot.SpringApplication.run(SpringApplication.java:1118) [spring-boot-1.5.10.RELEASE.jar!/:1.5.10.RELEASE]
        at org.springframework.boot.SpringApplication.run(SpringApplication.java:1107) [spring-boot-1.5.10.RELEASE.jar!/:1.5.10.RELEASE]
        at org.springframework.boot.SpringApplication$run.call(Unknown Source) [spring-boot-1.5.10.RELEASE.jar!/:1.5.10.RELEASE]
        at org.codehaus.groovy.runtime.callsite.CallSiteArray.defaultCall(CallSiteArray.java:47) [groovy-2.4.15.jar!/:2.4.15]
        at org.codehaus.groovy.runtime.callsite.AbstractCallSite.call(AbstractCallSite.java:116) [groovy-2.4.15.jar!/:2.4.15]
        at org.codehaus.groovy.runtime.callsite.AbstractCallSite.call(AbstractCallSite.java:136) [groovy-2.4.15.jar!/:2.4.15]
        at org.example.ssl.SSLExampleApplication.main(SSLExampleApplication.groovy:12) [classes!/:na]
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:1.8.0_171]
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[na:1.8.0_171]
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:1.8.0_171]
        at java.lang.reflect.Method.invoke(Method.java:498) ~[na:1.8.0_171]
        at org.springframework.boot.loader.MainMethodRunner.run(MainMethodRunner.java:48) [app.jar:na]
        at org.springframework.boot.loader.Launcher.launch(Launcher.java:87) [app.jar:na]
        at org.springframework.boot.loader.Launcher.launch(Launcher.java:50) [app.jar:na]
        at org.springframework.boot.loader.JarLauncher.main(JarLauncher.java:51) [app.jar:na]
Caused by: org.apache.catalina.LifecycleException: Protocol handler start failed
        at org.apache.catalina.connector.Connector.startInternal(Connector.java:1021) ~[tomcat-embed-core-8.5.27.jar!/:8.5.27]
        at org.apache.catalina.util.LifecycleBase.start(LifecycleBase.java:150) ~[tomcat-embed-core-8.5.27.jar!/:8.5.27]
        ... 25 common frames omitted
Caused by: java.lang.IllegalArgumentException: java.security.UnrecoverableKeyException: Get Key failed: Given final block not properly padded. Such issues can arise if a bad key is used during decryption.
        at org.apache.tomcat.util.net.AbstractJsseEndpoint.createSSLContext(AbstractJsseEndpoint.java:116) ~[tomcat-embed-core-8.5.27.jar!/:8.5.27]
        at org.apache.tomcat.util.net.AbstractJsseEndpoint.initialiseSsl(AbstractJsseEndpoint.java:87) ~[tomcat-embed-core-8.5.27.jar!/:8.5.27]
        at org.apache.tomcat.util.net.NioEndpoint.bind(NioEndpoint.java:225) ~[tomcat-embed-core-8.5.27.jar!/:8.5.27]
        at org.apache.tomcat.util.net.AbstractEndpoint.start(AbstractEndpoint.java:1150) ~[tomcat-embed-core-8.5.27.jar!/:8.5.27]
        at org.apache.coyote.AbstractProtocol.start(AbstractProtocol.java:591) ~[tomcat-embed-core-8.5.27.jar!/:8.5.27]
        at org.apache.catalina.connector.Connector.startInternal(Connector.java:1018) ~[tomcat-embed-core-8.5.27.jar!/:8.5.27]
        ... 26 common frames omitted
Caused by: java.security.UnrecoverableKeyException: Get Key failed: Given final block not properly padded. Such issues can arise if a bad key is used during decryption.
        at sun.security.pkcs12.PKCS12KeyStore.engineGetKey(PKCS12KeyStore.java:435) ~[na:1.8.0_171]
        at java.security.KeyStore.getKey(KeyStore.java:1023) ~[na:1.8.0_171]
        at org.apache.tomcat.util.net.jsse.JSSEUtil.getKeyManagers(JSSEUtil.java:242) ~[tomcat-embed-core-8.5.27.jar!/:8.5.27]
        at org.apache.tomcat.util.net.AbstractJsseEndpoint.createSSLContext(AbstractJsseEndpoint.java:114) ~[tomcat-embed-core-8.5.27.jar!/:8.5.27]
        ... 31 common frames omitted
Caused by: javax.crypto.BadPaddingException: Given final block not properly padded. Such issues can arise if a bad key is used during decryption.
        at com.sun.crypto.provider.CipherCore.doFinal(CipherCore.java:991) ~[sunjce_provider.jar:1.8.0_171]
        at com.sun.crypto.provider.CipherCore.doFinal(CipherCore.java:847) ~[sunjce_provider.jar:1.8.0_171]
        at com.sun.crypto.provider.PKCS12PBECipherCore.implDoFinal(PKCS12PBECipherCore.java:399) ~[sunjce_provider.jar:1.8.0_171]
        at com.sun.crypto.provider.PKCS12PBECipherCore$PBEWithSHA1AndDESede.engineDoFinal(PKCS12PBECipherCore.java:431) ~[sunjce_provider.jar:1.8.0_171]
        at javax.crypto.Cipher.doFinal(Cipher.java:2164) ~[na:1.8.0_171]
        at sun.security.pkcs12.PKCS12KeyStore.engineGetKey(PKCS12KeyStore.java:371) ~[na:1.8.0_171]
        ... 34 common frames omitted
```

If you remove the container:
```
docker rm example-ssl
```

You can then run the docker command with the same password for key store and private key:

```
docker run -i -p 8443:8443 -p 389:389 \
    -e 'SPRING_PROFILES_ACTIVE=Local,Container' \
    -e 'SSL_PORT=8443' \
    -e 'KEY_STORE_PASSWORD=keyStorePassword' \
    -e 'KEY_PASSWORD=keyStorePassword' \
    --name example-ssl -t org.example.ssl/example-ssl
```

and the application will start up properly. You can verify that the application is running by accessing:
```
https://localhost:8443/swagger-ui.html
```

User accounts can be found in the test-server.ldif under resources.
