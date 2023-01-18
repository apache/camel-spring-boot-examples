/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.example.mail.oauth2;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCredential;
import org.apache.camel.component.mail.MailAuthenticator;

import jakarta.mail.PasswordAuthentication;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Set;


public class Oauth2ExchangeMailAuthenticator extends MailAuthenticator {
    private static final Set<String> SCOPE = Collections.singleton("https://outlook.office365.com/.default");
    private static final String AUTHORITY_BASE_URL = "https://login.microsoftonline.com/";
    private final String clientId;
    private final String clientSecret;
    private final String authority;
    private final String user;


    public Oauth2ExchangeMailAuthenticator(String tenantId, String clientId, String clientSecret, String user) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authority = AUTHORITY_BASE_URL + tenantId;
        this.user = user;
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
           String accessToken = acquireToken();
            return new PasswordAuthentication(user, accessToken);
    }

    private String acquireToken() {

        ConfidentialClientApplication cca;
        try {
            // This is the secret that is created in the Azure portal when registering the application
            IClientCredential credential = ClientCredentialFactory.createFromSecret(clientSecret);

            cca = ConfidentialClientApplication
                    .builder(clientId, credential)
                    .authority(authority)
                    .build();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        // Client credential requests will by default try to look for a valid token in the
        // in-memory token cache. If found, it will return this token. If a token is not found, or the
        // token is not valid, it will fall back to acquiring a token from the AAD service. Although
        // not recommended unless there is a reason for doing so, you can skip the cache lookup
        // by using .skipCache(true) in ClientCredentialParameters.
        ClientCredentialParameters parameters =
                ClientCredentialParameters
                        .builder(SCOPE)
                        .build();

        IAuthenticationResult result = cca.acquireToken(parameters).join();
        return result.accessToken();
    }
}
