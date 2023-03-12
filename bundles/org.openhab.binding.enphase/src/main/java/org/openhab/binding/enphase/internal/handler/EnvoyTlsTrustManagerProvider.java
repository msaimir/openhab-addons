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
package org.openhab.binding.enphase.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.net.http.PEMTrustManager;
import org.openhab.core.io.net.http.TlsTrustManagerProvider;
import org.openhab.core.io.net.http.TrustAllTrustManager;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.X509ExtendedTrustManager;
import java.net.MalformedURLException;
import java.security.cert.CertificateException;

/**
 * @author Saimir Muco - Initial contribution
 */
@Component
@NonNullByDefault
public class EnvoyTlsTrustManagerProvider implements TlsTrustManagerProvider {
    private final String hostname;

    private final Logger logger = LoggerFactory.getLogger(EnvoyTlsTrustManagerProvider.class);

    public EnvoyTlsTrustManagerProvider(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public String getHostName() {
        return hostname;
    }

    @Override
    public X509ExtendedTrustManager getTrustManager() {
        logger.debug("Download self-signed certificate from {}", hostname);
        try {
            return PEMTrustManager.getInstanceFromServer("https://" + hostname);
        } catch (CertificateException | MalformedURLException e) {
            logger.debug("An unexpected exception occurred - returning a TrustAllTrustManager: {}", e.getMessage(), e);
        }
        return TrustAllTrustManager.getInstance();
    }
}
