export CLIENT_KEYSTORE_PASSWORD=password
export CLIENT_TRUSTSTORE_PASSWORD=password
export BROKER_KEYSTORE_PASSWORD=password
export BROKER_TRUSTSTORE_PASSWORD=password 

#Client Keystore
keytool -genkey -alias client -keyalg RSA -keystore client.ks -storepass $CLIENT_KEYSTORE_PASSWORD -keypass $CLIENT_KEYSTORE_PASSWORD -dname "CN=client, O=RedHat, C=UK"

#Broker Keystore
keytool -genkey -alias broker -keyalg RSA -keystore broker.ks -storepass $BROKER_KEYSTORE_PASSWORD -keypass $BROKER_KEYSTORE_PASSWORD -dname "CN=*.apps.cluster-hhl6r.hhl6r.sandbox55.opentlc.com, O=RedHat, C=UK"

#Export Client PublicKey
keytool -export -alias client -keystore client.ks -storepass $CLIENT_KEYSTORE_PASSWORD -file client.cert

#Export Server PublicKey
keytool -export -alias broker -keystore broker.ks -storepass $BROKER_KEYSTORE_PASSWORD -file broker.cert 

#Import Server PublicKey into Client Truststore
keytool -import -alias broker -keystore client.ts -file broker.cert -storepass $CLIENT_TRUSTSTORE_PASSWORD -trustcacerts -noprompt

#Import Client PublicKey into Server Truststore
keytool -import -alias client -keystore broker.ts -file client.cert -storepass $BROKER_TRUSTSTORE_PASSWORD -trustcacerts -noprompt

#Import Server PublicKey into Server Truststore (i.e.: trusts its self)
keytool -import -alias broker -keystore broker.ts -file broker.cert -storepass $BROKER_TRUSTSTORE_PASSWORD -trustcacerts -noprompt

