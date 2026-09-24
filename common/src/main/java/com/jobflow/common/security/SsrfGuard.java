package com.jobflow.common.security;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

public class SsrfGuard {

    public static class UnsafeUrlException extends RuntimeException {
        public UnsafeUrlException(String message) {
            super(message);
        }
    }

    /** Throws UnsafeUrlException if the URL is malformed, non-http(s), or resolves to a private/internal address. */
    public static void assertSafe(String urlString) {
        URI uri;
        try {
            uri = URI.create(urlString);
        } catch (IllegalArgumentException e) {
            throw new UnsafeUrlException("Malformed URL: " + urlString);
        }

        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new UnsafeUrlException("URL must use http or https");
        }

        String host = uri.getHost();
        if (host == null) {
            throw new UnsafeUrlException("URL must have a valid host");
        }

        InetAddress[] resolvedAddresses;
        try {
            // Resolves the hostname to its actual IP(s) right now - this is what lets us
            // catch DNS rebinding when called again immediately before dispatch
            resolvedAddresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            throw new UnsafeUrlException("Could not resolve host: " + host);
        }

        for (InetAddress address : resolvedAddresses) {
            if (isPrivateOrInternal(address)) {
                throw new UnsafeUrlException(
                        "URL resolves to a private or internal address, which is not allowed: " + address.getHostAddress());
            }
        }
    }

    private static boolean isPrivateOrInternal(InetAddress address) {
        return address.isLoopbackAddress()      // 127.0.0.0/8, ::1
                || address.isLinkLocalAddress()  // 169.254.0.0/16 (covers cloud metadata endpoints), fe80::/10
                || address.isSiteLocalAddress()  // 10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16
                || address.isAnyLocalAddress()   // 0.0.0.0
                || address.isMulticastAddress();
    }
}