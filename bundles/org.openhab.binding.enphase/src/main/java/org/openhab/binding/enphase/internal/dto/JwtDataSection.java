/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.enphase.internal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author Saimir Muco - Initial contribution
 */
public class JwtDataSection {

    @JsonProperty(value = "aud")
    private String serialNumber;
    @JsonProperty(value = "iss")
    private String issuer;
    private String enphaseUser;
    @JsonProperty(value = "exp")
    private long expires;
    @JsonProperty(value = "iat")
    private long issuerDate;
    private String jti;
    @JsonProperty(value = "username")
    private String userName;

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getEnphaseUser() {
        return enphaseUser;
    }

    public void setEnphaseUser(String enphaseUser) {
        this.enphaseUser = enphaseUser;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public void setIssuerDate(long issuerDate) {
        this.issuerDate = issuerDate;
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDateTime getExpires() {
        return convertToLocalDateTime(this.expires);
    }

    public LocalDateTime getIssuerDate() {
        return convertToLocalDateTime(this.issuerDate);
    }

    @Override
    public String toString() {
        return "JwtDataSection{" +
                "expires=" + expires +
                ", jti='" + jti + '\'' +
                '}';
    }

    private static LocalDateTime convertToLocalDateTime(long time) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time * 1000L), ZoneId.systemDefault());
    }
}
