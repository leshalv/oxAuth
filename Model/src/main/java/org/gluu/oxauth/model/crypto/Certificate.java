/*
 * oxAuth is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.model.crypto;

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.AsymmetricPublicKey;
import org.bouncycastle.crypto.asymmetric.AsymmetricECPublicKey;
import org.bouncycastle.crypto.asymmetric.AsymmetricRSAPublicKey;
import org.bouncycastle.crypto.fips.FipsEC;
import org.bouncycastle.crypto.fips.FipsRSA;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.gluu.oxauth.model.crypto.signature.AlgorithmFamily;
import org.gluu.oxauth.model.crypto.signature.ECDSAPublicKey;
import org.gluu.oxauth.model.crypto.signature.RSAPublicKey;
import org.gluu.oxauth.model.crypto.signature.SignatureAlgorithm;
import org.gluu.oxauth.model.util.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;

/**
 * @author Javier Rojas Blum
 * @version June 29, 2016
 */
public class Certificate {

    private SignatureAlgorithm signatureAlgorithm;
    private X509Certificate x509Certificate;

    public Certificate(SignatureAlgorithm signatureAlgorithm, X509Certificate x509Certificate) {
        this.signatureAlgorithm = signatureAlgorithm;
        this.x509Certificate = x509Certificate;
    }

    public PublicKey getPublicKey() {
        PublicKey publicKey = null;
        SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(x509Certificate.getPublicKey().getEncoded());
        //TODO:check this instanceOf
        if (x509Certificate != null && x509Certificate.getPublicKey() instanceof RSAPublicKey) {
        	
        	AsymmetricRSAPublicKey jcersaPublicKey = new AsymmetricRSAPublicKey(FipsRSA.ALGORITHM,subPubKeyInfo);
            publicKey = new RSAPublicKey(jcersaPublicKey.getModulus(), jcersaPublicKey.getPublicExponent());
        } else if (x509Certificate != null && x509Certificate.getPublicKey() instanceof ECPublicKey) {
        	
        	
        	AsymmetricECPublicKey jceecPublicKey = new AsymmetricECPublicKey(FipsEC.ALGORITHM, subPubKeyInfo);

            publicKey = new ECDSAPublicKey(signatureAlgorithm, jceecPublicKey.getW().getXCoord().toBigInteger(),
                    jceecPublicKey.getW().getYCoord().toBigInteger());
        }

        return publicKey;
    }

    public RSAPublicKey getRsaPublicKey() {
        RSAPublicKey rsaPublicKey = null;
        SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(x509Certificate.getPublicKey().getEncoded());
        //TODO:check the lines below
        if (x509Certificate != null && x509Certificate.getPublicKey() instanceof RSAPublicKey) {
        	
        	AsymmetricRSAPublicKey publicKey = new AsymmetricRSAPublicKey(FipsRSA.ALGORITHM,subPubKeyInfo);
            rsaPublicKey = new RSAPublicKey(publicKey.getModulus(), publicKey.getPublicExponent());
        }

        return rsaPublicKey;
    }

    public ECDSAPublicKey getEcdsaPublicKey() {
        ECDSAPublicKey ecdsaPublicKey = null;
        SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(x509Certificate.getPublicKey().getEncoded());
        //TODO:check these lines below
        if (x509Certificate != null && x509Certificate.getPublicKey() instanceof ECPublicKey) {
        	AsymmetricECPublicKey publicKey = new AsymmetricECPublicKey(FipsEC.ALGORITHM, subPubKeyInfo);
            ecdsaPublicKey = new ECDSAPublicKey(signatureAlgorithm, publicKey.getW().getXCoord().toBigInteger(),
                    publicKey.getW().getYCoord().toBigInteger());
        }

        return ecdsaPublicKey;
    }

    public JSONArray toJSONArray() throws JSONException {
        String cert = toString();

        cert = cert.replace("\n", "");
        cert = cert.replace("-----BEGIN CERTIFICATE-----", "");
        cert = cert.replace("-----END CERTIFICATE-----", "");

        return new JSONArray(Arrays.asList(cert));
    }

    @Override
    public String toString() {
        try {
            StringWriter stringWriter = new StringWriter();
            JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter);
            try {
                pemWriter.writeObject(x509Certificate);
                pemWriter.flush();
                return stringWriter.toString();
            } finally {
                pemWriter.close();
            }
        } catch (IOException e) {
            return StringUtils.EMPTY_STRING;
        } catch (Exception e) {
            return StringUtils.EMPTY_STRING;
        }
    }
}