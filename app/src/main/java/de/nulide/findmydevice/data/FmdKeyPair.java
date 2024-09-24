package de.nulide.findmydevice.data;

import java.security.KeyPair;
import java.security.PublicKey;

import de.nulide.findmydevice.utils.CypherUtils;

public class FmdKeyPair {
    private PublicKey publicKey;
    private String encryptedPrivateKey;

    // TODO make private
    public FmdKeyPair(PublicKey publicKey, String encryptedPrivateKey) {
        this.publicKey = publicKey;
        this.encryptedPrivateKey = encryptedPrivateKey;
    }

    public static FmdKeyPair generateNewFmdKeyPair(String passwordProtectKeyPairWith) {
        KeyPair rsaKeyPair = CypherUtils.genRsaKeyPair();
        String encryptedPrivateKey = CypherUtils.encryptPrivateKeyWithPassword(rsaKeyPair.getPrivate(), passwordProtectKeyPairWith);
        return new FmdKeyPair(rsaKeyPair.getPublic(), encryptedPrivateKey);
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public String getEncryptedPrivateKey() {
        return this.encryptedPrivateKey;
    }

    public String getBase64PublicKey() {
        return CypherUtils.encodeBase64(publicKey.getEncoded());
    }

    public void setEncryptedPrivateKey(String encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
    }
}
