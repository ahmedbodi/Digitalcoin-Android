/*
 * Copyright 2012 the original author or authors.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.google.digitalcoin.uri;

import com.google.digitalcoin.core.Address;
import com.google.digitalcoin.core.AddressFormatException;
import com.google.digitalcoin.core.NetworkParameters;
import com.google.digitalcoin.core.Utils;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

public class DigitalcoinURITest {

    private DigitalcoinURI testObject = null;

    private static final String PRODNET_GOOD_ADDRESS = "LQz2pJYaeqntA9BFB8rDX5AL2TTKGd5AuN";

    /**
     * Tests conversion to Digitalcoin URI
     * 
     * @throws DigitalcoinURIParseException
     *             If something goes wrong
     * @throws AddressFormatException 
     */
    @Test
    public void testConvertToDigitalcoinURI() throws Exception {
        Address goodAddress = new Address(NetworkParameters.prodNet(), PRODNET_GOOD_ADDRESS);
        
        // simple example
        assertEquals("digitalcoin:" + PRODNET_GOOD_ADDRESS + "?amount=12.34&label=Hello&message=AMessage", DigitalcoinURI.convertToDigitalcoinURI(goodAddress, Utils.toNanoCoins("12.34"), "Hello", "AMessage"));
        
        // example with spaces, ampersand and plus
        assertEquals("digitalcoin:" + PRODNET_GOOD_ADDRESS + "?amount=12.34&label=Hello%20World&message=Mess%20%26%20age%20%2B%20hope", DigitalcoinURI.convertToDigitalcoinURI(goodAddress, Utils.toNanoCoins("12.34"), "Hello World", "Mess & age + hope"));

        // amount negative
        try {
            DigitalcoinURI.convertToDigitalcoinURI(goodAddress, Utils.toNanoCoins("-0.1"), "hope", "glory");
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Amount must be positive"));
        }

        // no amount, label present, message present
        assertEquals("digitalcoin:" + PRODNET_GOOD_ADDRESS + "?label=Hello&message=glory", DigitalcoinURI.convertToDigitalcoinURI(goodAddress, null, "Hello", "glory"));
        
        // amount present, no label, message present
        assertEquals("digitalcoin:" + PRODNET_GOOD_ADDRESS + "?amount=0.1&message=glory", DigitalcoinURI.convertToDigitalcoinURI(goodAddress, Utils.toNanoCoins("0.1"), null, "glory"));
        assertEquals("digitalcoin:" + PRODNET_GOOD_ADDRESS + "?amount=0.1&message=glory", DigitalcoinURI.convertToDigitalcoinURI(goodAddress, Utils.toNanoCoins("0.1"), "", "glory"));

        // amount present, label present, no message
        assertEquals("digitalcoin:" + PRODNET_GOOD_ADDRESS + "?amount=12.34&label=Hello", DigitalcoinURI.convertToDigitalcoinURI(goodAddress,Utils.toNanoCoins("12.34"), "Hello", null));
        assertEquals("digitalcoin:" + PRODNET_GOOD_ADDRESS + "?amount=12.34&label=Hello", DigitalcoinURI.convertToDigitalcoinURI(goodAddress, Utils.toNanoCoins("12.34"), "Hello", ""));
              
        // amount present, no label, no message
        assertEquals("digitalcoin:" + PRODNET_GOOD_ADDRESS + "?amount=1000", DigitalcoinURI.convertToDigitalcoinURI(goodAddress, Utils.toNanoCoins("1000"), null, null));
        assertEquals("digitalcoin:" + PRODNET_GOOD_ADDRESS + "?amount=1000", DigitalcoinURI.convertToDigitalcoinURI(goodAddress, Utils.toNanoCoins("1000"), "", ""));
        
        // no amount, label present, no message
        assertEquals("digitalcoin:" + PRODNET_GOOD_ADDRESS + "?label=Hello", DigitalcoinURI.convertToDigitalcoinURI(goodAddress, null, "Hello", null));
        
        // no amount, no label, message present
        assertEquals("digitalcoin:" + PRODNET_GOOD_ADDRESS + "?message=Agatha", DigitalcoinURI.convertToDigitalcoinURI(goodAddress, null, null, "Agatha"));
        assertEquals("digitalcoin:" + PRODNET_GOOD_ADDRESS + "?message=Agatha", DigitalcoinURI.convertToDigitalcoinURI(goodAddress, null, "", "Agatha"));
      
        // no amount, no label, no message
        assertEquals("digitalcoin:" + PRODNET_GOOD_ADDRESS, DigitalcoinURI.convertToDigitalcoinURI(goodAddress, null, null, null));
        assertEquals("digitalcoin:" + PRODNET_GOOD_ADDRESS, DigitalcoinURI.convertToDigitalcoinURI(goodAddress, null, "", ""));
    }

    /**
     * Test the simplest well-formed URI
     * 
     * @throws DigitalcoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Simple() throws DigitalcoinURIParseException {
        testObject = new DigitalcoinURI(NetworkParameters.prodNet(), DigitalcoinURI.BITCOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS);
        assertNotNull(testObject);
        assertNull("Unexpected amount", testObject.getAmount());
        assertNull("Unexpected label", testObject.getLabel());
        assertEquals("Unexpected label", 20, testObject.getAddress().getHash160().length);
    }

    /**
     * Test a broken URI (bad scheme)
     */
    @Test
    public void testBad_Scheme() {
        try {
            testObject = new DigitalcoinURI(NetworkParameters.prodNet(), "blimpcoin:" + PRODNET_GOOD_ADDRESS);
            fail("Expecting DigitalcoinURIParseException");
        } catch (DigitalcoinURIParseException e) {
        }
    }

    /**
     * Test a broken URI (bad syntax)
     */
    @Test
    public void testBad_BadSyntax() {
        // Various illegal characters
        try {
            testObject = new DigitalcoinURI(NetworkParameters.prodNet(), DigitalcoinURI.BITCOIN_SCHEME + "|" + PRODNET_GOOD_ADDRESS);
            fail("Expecting DigitalcoinURIParseException");
        } catch (DigitalcoinURIParseException e) {
            assertTrue(e.getMessage().contains("Bad URI syntax"));
        }

        try {
            testObject = new DigitalcoinURI(NetworkParameters.prodNet(), DigitalcoinURI.BITCOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS + "\\");
            fail("Expecting DigitalcoinURIParseException");
        } catch (DigitalcoinURIParseException e) {
            assertTrue(e.getMessage().contains("Bad URI syntax"));
        }

        // Separator without field
        try {
            testObject = new DigitalcoinURI(NetworkParameters.prodNet(), DigitalcoinURI.BITCOIN_SCHEME + ":");
            fail("Expecting DigitalcoinURIParseException");
        } catch (DigitalcoinURIParseException e) {
            assertTrue(e.getMessage().contains("Bad URI syntax"));
        }
    }

    /**
     * Test a broken URI (missing address)
     */
    @Test
    public void testBad_Address() {
        try {
            testObject = new DigitalcoinURI(NetworkParameters.prodNet(), DigitalcoinURI.BITCOIN_SCHEME);
            fail("Expecting DigitalcoinURIParseException");
        } catch (DigitalcoinURIParseException e) {
        }
    }

    /**
     * Test a broken URI (bad address type)
     */
    @Test
    public void testBad_IncorrectAddressType() {
        try {
            testObject = new DigitalcoinURI(NetworkParameters.testNet(), DigitalcoinURI.BITCOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS);
            fail("Expecting DigitalcoinURIParseException");
        } catch (DigitalcoinURIParseException e) {
            assertTrue(e.getMessage().contains("Bad address"));
        }
    }

    /**
     * Handles a simple amount
     * 
     * @throws DigitalcoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Amount() throws DigitalcoinURIParseException {
        // Test the decimal parsing
        testObject = new DigitalcoinURI(NetworkParameters.prodNet(), DigitalcoinURI.BITCOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                + "?amount=9876543210.12345678");
        assertEquals("987654321012345678", testObject.getAmount().toString());

        // Test the decimal parsing
        testObject = new DigitalcoinURI(NetworkParameters.prodNet(), DigitalcoinURI.BITCOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                + "?amount=.12345678");
        assertEquals("12345678", testObject.getAmount().toString());

        // Test the integer parsing
        testObject = new DigitalcoinURI(NetworkParameters.prodNet(), DigitalcoinURI.BITCOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                + "?amount=9876543210");
        assertEquals("987654321000000000", testObject.getAmount().toString());
    }

    /**
     * Handles a simple label
     * 
     * @throws DigitalcoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Label() throws DigitalcoinURIParseException {
        testObject = new DigitalcoinURI(NetworkParameters.prodNet(), DigitalcoinURI.BITCOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                + "?label=Hello%20World");
        assertEquals("Hello World", testObject.getLabel());
    }

    /**
     * Handles a simple label with an embedded ampersand and plus
     * 
     * @throws DigitalcoinURIParseException
     *             If something goes wrong
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testGood_LabelWithAmpersandAndPlus() throws DigitalcoinURIParseException, UnsupportedEncodingException {
        String testString = "Hello Earth & Mars + Venus";
        String encodedLabel = DigitalcoinURI.encodeURLString(testString);
        testObject = new DigitalcoinURI(NetworkParameters.prodNet(), DigitalcoinURI.BITCOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS + "?label="
                + encodedLabel);
        assertEquals(testString, testObject.getLabel());
    }

    /**
     * Handles a Russian label (Unicode test)
     * 
     * @throws DigitalcoinURIParseException
     *             If something goes wrong
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testGood_LabelWithRussian() throws DigitalcoinURIParseException, UnsupportedEncodingException {
        // Moscow in Russian in Cyrillic
        String moscowString = "\u041c\u043e\u0441\u043a\u0432\u0430";
        String encodedLabel = DigitalcoinURI.encodeURLString(moscowString); 
        testObject = new DigitalcoinURI(NetworkParameters.prodNet(), DigitalcoinURI.BITCOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS + "?label="
                + encodedLabel);
        assertEquals(moscowString, testObject.getLabel());
    }

    /**
     * Handles a simple message
     * 
     * @throws DigitalcoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Message() throws DigitalcoinURIParseException {
        testObject = new DigitalcoinURI(NetworkParameters.prodNet(), DigitalcoinURI.BITCOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                + "?message=Hello%20World");
        assertEquals("Hello World", testObject.getMessage());
    }

    /**
     * Handles various well-formed combinations
     * 
     * @throws DigitalcoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Combinations() throws DigitalcoinURIParseException {
        testObject = new DigitalcoinURI(NetworkParameters.prodNet(), DigitalcoinURI.BITCOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                + "?amount=9876543210&label=Hello%20World&message=Be%20well");
        assertEquals(
                "DigitalcoinURI['address'='1KzTSfqjF2iKCduwz59nv2uqh1W2JsTxZH','amount'='987654321000000000','label'='Hello World','message'='Be well']",
                testObject.toString());
    }

    /**
     * Handles a badly formatted amount field
     * 
     * @throws DigitalcoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testBad_Amount() throws DigitalcoinURIParseException {
        // Missing
        try {
            testObject = new DigitalcoinURI(NetworkParameters.prodNet(), DigitalcoinURI.BITCOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                    + "?amount=");
            fail("Expecting DigitalcoinURIParseException");
        } catch (DigitalcoinURIParseException e) {
            assertTrue(e.getMessage().contains("amount"));
        }

        // Non-decimal (BIP 21)
        try {
            testObject = new DigitalcoinURI(NetworkParameters.prodNet(), DigitalcoinURI.BITCOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                    + "?amount=12X4");
            fail("Expecting DigitalcoinURIParseException");
        } catch (DigitalcoinURIParseException e) {
            assertTrue(e.getMessage().contains("amount"));
        }
    }

    /**
     * Handles a badly formatted label field
     * 
     * @throws DigitalcoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testBad_Label() throws DigitalcoinURIParseException {
        try {
            testObject = new DigitalcoinURI(NetworkParameters.prodNet(), DigitalcoinURI.BITCOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                    + "?label=");
            fail("Expecting DigitalcoinURIParseException");
        } catch (DigitalcoinURIParseException e) {
            assertTrue(e.getMessage().contains("label"));
        }
    }

    /**
     * Handles a badly formatted message field
     * 
     * @throws DigitalcoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testBad_Message() throws DigitalcoinURIParseException {
        try {
            testObject = new DigitalcoinURI(NetworkParameters.prodNet(), DigitalcoinURI.BITCOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                    + "?message=");
            fail("Expecting DigitalcoinURIParseException");
        } catch (DigitalcoinURIParseException e) {
            assertTrue(e.getMessage().contains("message"));
        }
    }

    /**
     * Handles duplicated fields (sneaky address overwrite attack)
     * 
     * @throws DigitalcoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testBad_Duplicated() throws DigitalcoinURIParseException {
        try {
            testObject = new DigitalcoinURI(NetworkParameters.prodNet(), DigitalcoinURI.BITCOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                    + "?address=aardvark");
            fail("Expecting DigitalcoinURIParseException");
        } catch (DigitalcoinURIParseException e) {
            assertTrue(e.getMessage().contains("address"));
        }
    }

    /**
     * Handles case when there are too many equals
     * 
     * @throws DigitalcoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testBad_TooManyEquals() throws DigitalcoinURIParseException {
        try {
            testObject = new DigitalcoinURI(NetworkParameters.prodNet(), DigitalcoinURI.BITCOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                    + "?label=aardvark=zebra");
            fail("Expecting DigitalcoinURIParseException");
        } catch (DigitalcoinURIParseException e) {
            assertTrue(e.getMessage().contains("cannot parse name value pair"));
        }
    }

    /**
     * Handles case when there are too many question marks
     * 
     * @throws DigitalcoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testBad_TooManyQuestionMarks() throws DigitalcoinURIParseException {
        try {
            testObject = new DigitalcoinURI(NetworkParameters.prodNet(), DigitalcoinURI.BITCOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                    + "?label=aardvark?message=zebra");
            fail("Expecting DigitalcoinURIParseException");
        } catch (DigitalcoinURIParseException e) {
            assertTrue(e.getMessage().contains("Too many question marks"));
        }
    }
    
    /**
     * Handles unknown fields (required and not required)
     * 
     * @throws DigitalcoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testUnknown() throws DigitalcoinURIParseException {
        // Unknown not required field
        testObject = new DigitalcoinURI(NetworkParameters.prodNet(), DigitalcoinURI.BITCOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                + "?aardvark=true");
        assertEquals("DigitalcoinURI['address'='1KzTSfqjF2iKCduwz59nv2uqh1W2JsTxZH','aardvark'='true']", testObject.toString());

        assertEquals("true", (String) testObject.getParameterByName("aardvark"));

        // Unknown not required field (isolated)
        try {
            testObject = new DigitalcoinURI(NetworkParameters.prodNet(), DigitalcoinURI.BITCOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                    + "?aardvark");
            fail("Expecting DigitalcoinURIParseException");
        } catch (DigitalcoinURIParseException e) {
            assertTrue(e.getMessage().contains("cannot parse name value pair"));
        }

        // Unknown and required field
        try {
            testObject = new DigitalcoinURI(NetworkParameters.prodNet(), DigitalcoinURI.BITCOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                    + "?req-aardvark=true");
            fail("Expecting DigitalcoinURIParseException");
        } catch (DigitalcoinURIParseException e) {
            assertTrue(e.getMessage().contains("req-aardvark"));
        }
    }

    @Test
    public void brokenURIs() throws DigitalcoinURIParseException {
        // Check we can parse the incorrectly formatted URIs produced by blockchain.info and its iPhone app.
        String str = "digitalcoin://1KzTSfqjF2iKCduwz59nv2uqh1W2JsTxZH?amount=0.01000000";
        DigitalcoinURI uri = new DigitalcoinURI(str);
        assertEquals("1KzTSfqjF2iKCduwz59nv2uqh1W2JsTxZH", uri.getAddress().toString());
        assertEquals(Utils.toNanoCoins(0, 1), uri.getAmount());
    }
}
