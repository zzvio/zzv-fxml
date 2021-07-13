/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.util;

import java.util.Base64;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Basic authentication helper.
 *
 */
public class BasicAuth {

    /**
     * Parses the username and password from the AUTHORIZATION header.
     * 
     * @param auth
     * @return a pair of username and password if success, otherwise null
     */
    public static Pair<String, String> parseAuth(String auth) {
        try {
            if (auth != null && auth.startsWith("Basic ")) {
                String str = Bytes.toString(Base64.getDecoder().decode(auth.substring(6)));
                int idx = str.indexOf(':');
                if (idx != -1) {
                    return Pair.of(str.substring(0, idx), str.substring(idx + 1));
                }
            }
        } catch (IllegalArgumentException e) {
            // invalid base64 string
        }

        return null;
    }

    /**
     * Generates the AUTHORIZATION header.
     * 
     * @param username
     * @param password
     * @return
     */
    public static String generateAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString(Bytes.of(username + ":" + password));
    }

    private BasicAuth() {
    }
}
