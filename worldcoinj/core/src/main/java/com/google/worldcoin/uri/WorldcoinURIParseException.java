package com.google.digitalcoin.uri;

/**
 * <p>Exception to provide the following to {@link DigitalcoinURI}:</p>
 * <ul>
 * <li>Provision of parsing error messages</li>
 * </ul>
 * <p>This base exception acts as a general failure mode not attributable to a specific cause (other than
 * that reported in the exception message). Since this is in English, it may not be worth reporting directly
 * to the user other than as part of a "general failure to parse" response.</p>
 *
 * @since 0.4.0
 */
public class DigitalcoinURIParseException extends RuntimeException {
    public DigitalcoinURIParseException(String s) {
        super(s);
    }

    public DigitalcoinURIParseException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
