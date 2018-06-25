#!/usr/bin/env sh

# passwords for the local generated certs
export KEY_STORE_PASSWORD=keyStorePassword
export KEY_PASSWORD=keyPassword

# Pick which OpenSSL to use - system default or one installed with Homebrew
export OPENSSL=openssl
#export OPENSSL=/usr/local/opt/openssl/bin/openssl

# Clean any previously-generated keys or certs
rm -rf ./generated
mkdir ./generated

# Generate Root CA Certificate

$OPENSSL genrsa -aes256 \
                -out ./generated/ca.key.pem -passout env:KEY_PASSWORD \
                4096

$OPENSSL req -config openssl.ca.cnf -batch \
             -new -x509 -days 3650 \
             -key ./generated/ca.key.pem -passin env:KEY_PASSWORD \
             -out ./generated/ca.cert.pem -passout env:KEY_PASSWORD

# Generate Server Certificate signed by Root CA

$OPENSSL genrsa -aes256 \
                -out ./generated/localhost.key.pem -passout env:KEY_PASSWORD \
                4096

$OPENSSL req -config openssl.server.cnf -extensions server_cert -batch \
             -new -days 3650 \
             -key ./generated/localhost.key.pem -passin env:KEY_PASSWORD \
             -out ./generated/localhost.cert.csr -passout env:KEY_PASSWORD

$OPENSSL x509 -extfile openssl.server.cnf -extensions server_cert \
              -req -sha512 -days 3650 \
              -in ./generated/localhost.cert.csr -passin env:KEY_PASSWORD \
              -CAkey ./generated/ca.key.pem -CA ./generated/ca.cert.pem -set_serial 1000 \
              -out ./generated/localhost.cert.pem

# Convert Server Key and Certificate to proper format for java trust store

$OPENSSL pkcs12 -export -name localhost -caname "Localhost CA" \
                -inkey ./generated/localhost.key.pem -passin env:KEY_PASSWORD \
                -in ./generated/localhost.cert.pem \
                -certfile ./generated/ca.cert.pem \
                -out ./generated/localhost.p12 -passout env:KEY_STORE_PASSWORD
