/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.cketti.mailto;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class MailToTest {
    private static final String MAILTOURI_1 = "mailto:chris@example.com";
    private static final String MAILTOURI_2 = "mailto:infobot@example.com?subject=current-issue";
    private static final String MAILTOURI_3 =
            "mailto:infobot@example.com?body=send%20current-issue";
    private static final String MAILTOURI_4 = "mailto:infobot@example.com?body=send%20current-" +
            "issue%0D%0Asend%20index";
    private static final String MAILTOURI_5 = "mailto:joe@example.com?" +
            "cc=bob@example.com&body=hello";
    private static final String MAILTOURI_6 = "mailto:?to=joe@example.com&" +
            "cc=bob@example.com&body=hello";

    @Test
    public void testParseMailToURI() {
        assertFalse(MailTo.isMailTo(null));
        assertFalse(MailTo.isMailTo(""));
        assertFalse(MailTo.isMailTo("http://www.google.com"));

        assertTrue(MailTo.isMailTo(MAILTOURI_1));
        MailTo mailTo_1 = MailTo.parse(MAILTOURI_1);
        assertEquals("chris@example.com", mailTo_1.getTo());
        assertEquals(1, mailTo_1.getHeaders().size());
        assertNull(mailTo_1.getBody());
        assertNull(mailTo_1.getCc());
        assertNull(mailTo_1.getSubject());
        assertEquals("mailto:?to=chris%40example.com&", mailTo_1.toString());

        assertTrue(MailTo.isMailTo(MAILTOURI_2));
        MailTo mailTo_2 = MailTo.parse(MAILTOURI_2);
        assertEquals(2, mailTo_2.getHeaders().size());
        assertEquals("infobot@example.com", mailTo_2.getTo());
        assertEquals("current-issue", mailTo_2.getSubject());
        assertNull(mailTo_2.getBody());
        assertNull(mailTo_2.getCc());
        String stringUrl = mailTo_2.toString();
        assertTrue(stringUrl.startsWith("mailto:?"));
        assertTrue(stringUrl.contains("to=infobot%40example.com&"));
        assertTrue(stringUrl.contains("subject=current-issue&"));

        assertTrue(MailTo.isMailTo(MAILTOURI_3));
        MailTo mailTo_3 = MailTo.parse(MAILTOURI_3);
        assertEquals(2, mailTo_3.getHeaders().size());
        assertEquals("infobot@example.com", mailTo_3.getTo());
        assertEquals("send current-issue", mailTo_3.getBody());
        assertNull(mailTo_3.getCc());
        assertNull(mailTo_3.getSubject());
        stringUrl = mailTo_3.toString();
        assertTrue(stringUrl.startsWith("mailto:?"));
        assertTrue(stringUrl.contains("to=infobot%40example.com&"));
        assertTrue(stringUrl.contains("body=send%20current-issue&"));

        assertTrue(MailTo.isMailTo(MAILTOURI_4));
        MailTo mailTo_4 = MailTo.parse(MAILTOURI_4);
        assertEquals(2, mailTo_4.getHeaders().size());
        assertEquals("infobot@example.com", mailTo_4.getTo());
        assertEquals("send current-issue\r\nsend index", mailTo_4.getBody());
        assertNull(mailTo_4.getCc());
        assertNull(mailTo_4.getSubject());
        stringUrl = mailTo_4.toString();
        assertTrue(stringUrl.startsWith("mailto:?"));
        assertTrue(stringUrl.contains("to=infobot%40example.com&"));
        assertTrue(stringUrl.contains("body=send%20current-issue%0D%0Asend%20index&"));


        assertTrue(MailTo.isMailTo(MAILTOURI_5));
        MailTo mailTo_5 = MailTo.parse(MAILTOURI_5);
        assertEquals(3, mailTo_5.getHeaders().size());
        assertEquals("joe@example.com", mailTo_5.getTo());
        assertEquals("bob@example.com", mailTo_5.getCc());
        assertEquals("hello", mailTo_5.getBody());
        assertNull(mailTo_5.getSubject());
        stringUrl = mailTo_5.toString();
        assertTrue(stringUrl.startsWith("mailto:?"));
        assertTrue(stringUrl.contains("cc=bob%40example.com&"));
        assertTrue(stringUrl.contains("body=hello&"));
        assertTrue(stringUrl.contains("to=joe%40example.com&"));

        assertTrue(MailTo.isMailTo(MAILTOURI_6));
        MailTo mailTo_6 = MailTo.parse(MAILTOURI_6);
        assertEquals(3, mailTo_6.getHeaders().size());
        assertEquals(", joe@example.com", mailTo_6.getTo());
        assertEquals("bob@example.com", mailTo_6.getCc());
        assertEquals("hello", mailTo_6.getBody());
        assertNull(mailTo_6.getSubject());
        stringUrl = mailTo_6.toString();
        assertTrue(stringUrl.startsWith("mailto:?"));
        assertTrue(stringUrl.contains("cc=bob%40example.com&"));
        assertTrue(stringUrl.contains("body=hello&"));
        assertTrue(stringUrl.contains("to=%2C%20joe%40example.com&"));
    }

    @Test
    public void encodedAmpersandInBody() {
        MailTo mailTo = MailTo.parse("mailto:alice@example.com?body=a%26b");

        assertEquals("a&b", mailTo.getBody());
    }

    @Test
    public void encodedEqualSignInBody() {
        MailTo mailTo = MailTo.parse("mailto:alice@example.com?body=a%3Db");

        assertEquals("a=b", mailTo.getBody());
    }

    @Test
    public void unencodedEqualsSignInBody() {
        // This is not a properly encoded mailto URI. But there's no good reason to drop everything
        // after the equals sign in the 'body' query parameter value.
        MailTo mailTo = MailTo.parse("mailto:alice@example.com?body=foo=bar&subject=test");

        assertEquals("foo=bar", mailTo.getBody());
        assertEquals("test", mailTo.getSubject());
    }

    @Test
    public void encodedPercentValueInBody() {
        MailTo mailTo = MailTo.parse("mailto:alice@example.com?body=%2525");

        assertEquals("%25", mailTo.getBody());
    }

    @Test
    public void colonInBody() {
        MailTo mailTo = MailTo.parse("mailto:alice@example.com?body=one:two");

        assertEquals("one:two", mailTo.getBody());
    }
}
