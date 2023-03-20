/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openhab.binding.enphase.internal.EnvoyConnectionException;
import org.openhab.binding.enphase.internal.dto.WebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author Saimir Muco - Initial contribution
 */
@NonNullByDefault
public class EnphaseJWTExtractor {

    private final static Logger logger = LoggerFactory.getLogger(EnphaseJWTExtractor.class);

    private EnphaseJWTExtractor() {
        throw new IllegalStateException("Utility class");
    }

    public static String fetchJWT(HttpClient httpClient, String baseUri, String username, String password, String serialNumber) throws EnvoyConnectionException {
        try {
            logger.trace("Fetching Enlighten Login Page");
            Document loginPage = getLoginPage(httpClient, baseUri);
            logger.trace("Attempting to Login with a Form Submit");
            postForm(httpClient, baseUri, encodeFormData(loginPage, username, password));
            logger.trace("Fetching and Scanning returned HTML for Token");
            return scanForToken(httpClient, baseUri, serialNumber);
        } catch (ExecutionException | TimeoutException | JsonProcessingException e) {
            throw new EnvoyConnectionException("Error retrieving jwt token from enphase", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EnvoyConnectionException("Interrupt error retrieving jwt token from enphase", e);
        }
    }

    private static String findInputValue(List<Element> inputElements, String name) {
        return inputElements.stream()
                .filter(inputElement -> inputElement.attr("name").equalsIgnoreCase(name))
                .findFirst()
                .map(element -> element.attr("value"))
                .orElse("");
    }

    private static FormContentProvider encodeFormData(Document document, String username, String password) {
        List<Element> inputElements = document.getElementsByTag("input");
        Fields fields = new Fields();
        fields.put("utf8", findInputValue(inputElements, "utf8"));
        fields.put("authenticity_token", findInputValue(inputElements, "authenticity_token"));
        fields.put("user[email]", username);
        fields.put("user[password]", password);
        fields.put("commit", findInputValue(inputElements, "commit"));
        return new FormContentProvider(fields);
    }

    private static String scanForToken(HttpClient httpClient, String baseUri, String serialNumber) throws ExecutionException, InterruptedException, TimeoutException, JsonProcessingException, EnvoyConnectionException {
        var response = httpClient.newRequest(baseUri + "/entrez-auth-token?serial_num=" + serialNumber).send();
        Document jwt = Jsoup.parse(response.getContentAsString(), baseUri);
        String tokenObject = jwt.getElementsByTag("body").text();
        if (tokenObject.isEmpty()) {
            logger.error("Scan for token failed.  HTML fetched = {}", response.getContentAsString());
            throw new EnvoyConnectionException("Failed to fetch the token page");
        }
        ObjectMapper jsonMapper = new ObjectMapper();
        return jsonMapper.readValue(tokenObject, WebToken.class).getToken();
    }

    private static Document getLoginPage(HttpClient httpClient, String url) throws ExecutionException, InterruptedException, TimeoutException {
        var request = httpClient.newRequest(url);
        request.header(HttpHeader.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
        var response = request.send();
        return Jsoup.parse(response.getContentAsString(), url);
    }

    private static void postForm(HttpClient httpClient, String baseUri, FormContentProvider formContentProvider) throws EnvoyConnectionException, ExecutionException, InterruptedException, TimeoutException {
        ContentResponse response;

        response = httpClient.newRequest(baseUri + "/login/login").method(HttpMethod.POST)
                .header(HttpHeader.ORIGIN, baseUri)
                .header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .content(formContentProvider)
                .send();

        if (response.getStatus() != 200) {
            throw new EnvoyConnectionException("Could not authenticate to enlighten envoy web application");
        }
    }
}
