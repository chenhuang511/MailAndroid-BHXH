package vn.bhxh.bhxhmail.message.extractors;


import org.junit.Before;
import org.junit.Test;

import vn.bhxh.bhxhmail.mail.BodyPart;
import vn.bhxh.bhxhmail.mail.Part;
import vn.bhxh.bhxhmail.message.MessageCreationHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class TextPartFinderTest {
    private TextPartFinder textPartFinder;


    @Before
    public void setUp() throws Exception {
        textPartFinder = new TextPartFinder();
    }

    @Test
    public void findFirstTextPart_withTextPlainPart() throws Exception {
        Part part = MessageCreationHelper.createTextPart("text/plain");

        Part result = textPartFinder.findFirstTextPart(part);

        assertEquals(part, result);
    }

    @Test
    public void findFirstTextPart_withTextHtmlPart() throws Exception {
        Part part = MessageCreationHelper.createTextPart("text/html");

        Part result = textPartFinder.findFirstTextPart(part);

        assertEquals(part, result);
    }

    @Test
    public void findFirstTextPart_withoutTextPart() throws Exception {
        Part part = MessageCreationHelper.createPart("image/jpeg");

        Part result = textPartFinder.findFirstTextPart(part);

        assertNull(result);
    }

    @Test
    public void findFirstTextPart_withMultipartAlternative() throws Exception {
        BodyPart expected = MessageCreationHelper.createTextPart("text/plain");
        Part part = MessageCreationHelper.createMultipart("multipart/alternative", expected, MessageCreationHelper.createTextPart("text/html"));

        Part result = textPartFinder.findFirstTextPart(part);

        assertEquals(expected, result);
    }

    @Test
    public void findFirstTextPart_withMultipartAlternativeHtmlPartFirst() throws Exception {
        BodyPart expected = MessageCreationHelper.createTextPart("text/plain");
        Part part = MessageCreationHelper.createMultipart("multipart/alternative", MessageCreationHelper.createTextPart("text/html"), expected);

        Part result = textPartFinder.findFirstTextPart(part);

        assertEquals(expected, result);
    }

    @Test
    public void findFirstTextPart_withMultipartAlternativeContainingOnlyTextHtmlPart() throws Exception {
        BodyPart expected = MessageCreationHelper.createTextPart("text/html");
        Part part = MessageCreationHelper.createMultipart("multipart/alternative",
                MessageCreationHelper.createPart("image/gif"),
                expected,
                MessageCreationHelper.createTextPart("text/html"));

        Part result = textPartFinder.findFirstTextPart(part);

        assertEquals(expected, result);
    }

    @Test
    public void findFirstTextPart_withMultipartAlternativeNotContainingTextPart() throws Exception {
        Part part = MessageCreationHelper.createMultipart("multipart/alternative",
                MessageCreationHelper.createPart("image/gif"),
                MessageCreationHelper.createPart("application/pdf"));

        Part result = textPartFinder.findFirstTextPart(part);

        assertNull(result);
    }

    @Test
    public void findFirstTextPart_withMultipartAlternativeContainingMultipartRelatedContainingTextPlain()
            throws Exception {
        BodyPart expected = MessageCreationHelper.createTextPart("text/plain");
        Part part = MessageCreationHelper.createMultipart("multipart/alternative",
                MessageCreationHelper.createMultipart("multipart/related", expected, MessageCreationHelper.createPart("image/jpeg")),
                MessageCreationHelper.createTextPart("text/html"));

        Part result = textPartFinder.findFirstTextPart(part);

        assertEquals(expected, result);
    }

    @Test
    public void findFirstTextPart_withMultipartAlternativeContainingMultipartRelatedContainingTextHtmlFirst()
            throws Exception {
        BodyPart expected = MessageCreationHelper.createTextPart("text/plain");
        Part part = MessageCreationHelper.createMultipart("multipart/alternative",
                MessageCreationHelper.createMultipart("multipart/related", MessageCreationHelper.createTextPart("text/html"), MessageCreationHelper.createPart("image/jpeg")),
                expected);

        Part result = textPartFinder.findFirstTextPart(part);

        assertEquals(expected, result);
    }

    @Test
    public void findFirstTextPart_withMultipartMixedContainingTextPlain() throws Exception {
        BodyPart expected = MessageCreationHelper.createTextPart("text/plain");
        Part part = MessageCreationHelper.createMultipart("multipart/mixed", MessageCreationHelper.createPart("image/jpeg"), expected);

        Part result = textPartFinder.findFirstTextPart(part);

        assertEquals(expected, result);
    }

    @Test
    public void findFirstTextPart_withMultipartMixedContainingTextHtmlFirst() throws Exception {
        BodyPart expected = MessageCreationHelper.createTextPart("text/html");
        Part part = MessageCreationHelper.createMultipart("multipart/mixed", expected, MessageCreationHelper.createTextPart("text/plain"));

        Part result = textPartFinder.findFirstTextPart(part);

        assertEquals(expected, result);
    }

    @Test
    public void findFirstTextPart_withMultipartMixedNotContainingTextPart() throws Exception {
        Part part = MessageCreationHelper.createMultipart("multipart/mixed", MessageCreationHelper.createPart("image/jpeg"), MessageCreationHelper.createPart("image/gif"));

        Part result = textPartFinder.findFirstTextPart(part);

        assertNull(result);
    }

    @Test
    public void findFirstTextPart_withMultipartMixedContainingMultipartAlternative() throws Exception {
        BodyPart expected = MessageCreationHelper.createTextPart("text/plain");
        Part part = MessageCreationHelper.createMultipart("multipart/mixed",
                MessageCreationHelper.createPart("image/jpeg"),
                MessageCreationHelper.createMultipart("multipart/alternative", expected, MessageCreationHelper.createTextPart("text/html")),
                MessageCreationHelper.createTextPart("text/plain"));

        Part result = textPartFinder.findFirstTextPart(part);

        assertEquals(expected, result);
    }

    @Test
    public void findFirstTextPart_withMultipartMixedContainingMultipartAlternativeWithTextPlainPartLast()
            throws Exception {
        BodyPart expected = MessageCreationHelper.createTextPart("text/plain");
        Part part = MessageCreationHelper.createMultipart("multipart/mixed",
                MessageCreationHelper.createMultipart("multipart/alternative", MessageCreationHelper.createTextPart("text/html"), expected));

        Part result = textPartFinder.findFirstTextPart(part);

        assertEquals(expected, result);
    }

    @Test
    public void findFirstTextPart_withMultipartAlternativeContainingEmptyTextPlainPart()
            throws Exception {
        BodyPart expected = MessageCreationHelper.createEmptyPart("text/plain");
        Part part = MessageCreationHelper.createMultipart("multipart/alternative", expected, MessageCreationHelper.createTextPart("text/html"));

        Part result = textPartFinder.findFirstTextPart(part);

        assertEquals(expected, result);
    }

    @Test
    public void findFirstTextPart_withMultipartMixedContainingEmptyTextHtmlPart()
            throws Exception {
        BodyPart expected = MessageCreationHelper.createEmptyPart("text/html");
        Part part = MessageCreationHelper.createMultipart("multipart/mixed", expected, MessageCreationHelper.createTextPart("text/plain"));

        Part result = textPartFinder.findFirstTextPart(part);

        assertEquals(expected, result);
    }
}
