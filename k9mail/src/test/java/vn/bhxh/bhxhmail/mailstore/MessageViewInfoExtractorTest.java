package vn.bhxh.bhxhmail.mailstore;


import android.app.Application;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import vn.bhxh.bhxhmail.GlobalsHelper;
import vn.bhxh.bhxhmail.activity.K9ActivityCommon;
import vn.bhxh.bhxhmail.helper.HtmlSanitizer;
import vn.bhxh.bhxhmail.helper.HtmlSanitizerHelper;
import vn.bhxh.bhxhmail.mail.Address;
import vn.bhxh.bhxhmail.mail.Message;
import vn.bhxh.bhxhmail.mail.MessagingException;
import vn.bhxh.bhxhmail.mail.Part;
import vn.bhxh.bhxhmail.mail.internet.MessageExtractor;
import vn.bhxh.bhxhmail.mail.internet.MimeBodyPart;
import vn.bhxh.bhxhmail.mail.internet.MimeHeader;
import vn.bhxh.bhxhmail.mail.internet.MimeMessage;
import vn.bhxh.bhxhmail.mail.internet.MimeMessageHelper;
import vn.bhxh.bhxhmail.mail.internet.MimeMultipart;
import vn.bhxh.bhxhmail.mail.internet.TextBody;
import vn.bhxh.bhxhmail.mail.internet.Viewable;
import vn.bhxh.bhxhmail.mailstore.MessageViewInfoExtractor.ViewableExtractedText;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml", sdk = 21)
public class MessageViewInfoExtractorTest {
    public static final String BODY_TEXT = "K-9 Mail rocks :>";
    public static final String BODY_TEXT_HTML = "K-9 Mail rocks :&gt;";


    private MessageViewInfoExtractor messageViewInfoExtractor;
    private Application context;


    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;

        GlobalsHelper.setContext(context);

        HtmlSanitizer dummyHtmlSanitizer = HtmlSanitizerHelper.getDummyHtmlSanitizer();

        messageViewInfoExtractor = new MessageViewInfoExtractor(context,
                null, dummyHtmlSanitizer);
    }

    @Test
    public void testShouldSanitizeOutputHtml() throws MessagingException {
        // Create text/plain body
        TextBody body = new TextBody(BODY_TEXT);

        // Create message
        MimeMessage message = new MimeMessage();
        MimeMessageHelper.setBody(message, body);

        // Prepare fixture
        HtmlSanitizer htmlSanitizer = mock(HtmlSanitizer.class);
        MessageViewInfoExtractor messageViewInfoExtractor =
                new MessageViewInfoExtractor(context, null, htmlSanitizer);
        String value = "--sanitized html--";
        when(htmlSanitizer.sanitize(any(String.class))).thenReturn(value);

        // Extract text
        List<Part> outputNonViewableParts = new ArrayList<>();
        ArrayList<Viewable> outputViewableParts = new ArrayList<>();
        MessageExtractor.findViewablesAndAttachments(message, outputViewableParts, outputNonViewableParts);
        ViewableExtractedText viewableExtractedText =
                messageViewInfoExtractor.extractTextFromViewables(outputViewableParts);

        assertSame(value, viewableExtractedText.html);
    }

    @Test
    public void testSimplePlainTextMessage() throws MessagingException {
        // Create text/plain body
        TextBody body = new TextBody(BODY_TEXT);

        // Create message
        MimeMessage message = new MimeMessage();
        message.setHeader(MimeHeader.HEADER_CONTENT_TYPE, "text/plain");
        MimeMessageHelper.setBody(message, body);

        // Extract text
        List<Part> outputNonViewableParts = new ArrayList<>();
        ArrayList<Viewable> outputViewableParts = new ArrayList<>();
        MessageExtractor.findViewablesAndAttachments(message, outputViewableParts, outputNonViewableParts);
        ViewableExtractedText container = messageViewInfoExtractor.extractTextFromViewables(outputViewableParts);

        String expectedText = BODY_TEXT;
        String expectedHtml =
                "<pre class=\"k9mail\">" +
                "K-9 Mail rocks :&gt;" +
                "</pre>";

        assertEquals(expectedText, container.text);
        assertEquals(expectedHtml, getHtmlBodyText(container.html));
    }

    @Test
    public void testSimpleHtmlMessage() throws MessagingException {
        String bodyText = "<strong>K-9 Mail</strong> rocks :&gt;";

        // Create text/plain body
        TextBody body = new TextBody(bodyText);

        // Create message
        MimeMessage message = new MimeMessage();
        message.setHeader(MimeHeader.HEADER_CONTENT_TYPE, "text/html");
        MimeMessageHelper.setBody(message, body);

        // Extract text
        ArrayList<Viewable> outputViewableParts = new ArrayList<>();
        MessageExtractor.findViewablesAndAttachments(message, outputViewableParts, null);
        assertEquals(outputViewableParts.size(), 1);
        ViewableExtractedText container = messageViewInfoExtractor.extractTextFromViewables(outputViewableParts);

        String expectedText = BODY_TEXT;
        String expectedHtml =
                bodyText;

        assertEquals(expectedText, container.text);
        assertEquals(expectedHtml, getHtmlBodyText(container.html));
    }

    @Test
    public void testMultipartPlainTextMessage() throws MessagingException {
        String bodyText1 = "text body 1";
        String bodyText2 = "text body 2";

        // Create text/plain bodies
        TextBody body1 = new TextBody(bodyText1);
        TextBody body2 = new TextBody(bodyText2);

        // Create multipart/mixed part
        MimeMultipart multipart = MimeMultipart.newInstance();
        MimeBodyPart bodyPart1 = new MimeBodyPart(body1, "text/plain");
        MimeBodyPart bodyPart2 = new MimeBodyPart(body2, "text/plain");
        multipart.addBodyPart(bodyPart1);
        multipart.addBodyPart(bodyPart2);

        // Create message
        MimeMessage message = new MimeMessage();
        MimeMessageHelper.setBody(message, multipart);

        // Extract text
        List<Part> outputNonViewableParts = new ArrayList<>();
        ArrayList<Viewable> outputViewableParts = new ArrayList<>();
        MessageExtractor.findViewablesAndAttachments(message, outputViewableParts, outputNonViewableParts);
        ViewableExtractedText container = messageViewInfoExtractor.extractTextFromViewables(outputViewableParts);

        String expectedText =
                bodyText1 + "\r\n\r\n" +
                "------------------------------------------------------------------------\r\n\r\n" +
                bodyText2;
        String expectedHtml =
                "<pre class=\"k9mail\">" +
                bodyText1 +
                "</pre>" +
                "<p style=\"margin-top: 2.5em; margin-bottom: 1em; " +
                        "border-bottom: 1px solid #000\"></p>" +
                "<pre class=\"k9mail\">" +
                bodyText2 +
                "</pre>";


        assertEquals(expectedText, container.text);
        assertEquals(expectedHtml, getHtmlBodyText(container.html));
    }

    @Test
    public void testTextPlusRfc822Message() throws MessagingException {
        K9ActivityCommon.setLanguage(context, "en");
        Locale.setDefault(Locale.US);
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+01:00"));

        String innerBodyText = "Hey there. I'm inside a message/rfc822 (inline) attachment.";

        // Create text/plain body
        TextBody textBody = new TextBody(BODY_TEXT);

        // Create inner text/plain body
        TextBody innerBody = new TextBody(innerBodyText);

        // Create message/rfc822 body
        MimeMessage innerMessage = new MimeMessage();
        innerMessage.addSentDate(new Date(112, 02, 17), false);
        innerMessage.setRecipients(Message.RecipientType.TO, new Address[] { new Address("to@example.com") });
        innerMessage.setSubject("Subject");
        innerMessage.setFrom(new Address("from@example.com"));
        MimeMessageHelper.setBody(innerMessage, innerBody);

        // Create multipart/mixed part
        MimeMultipart multipart = MimeMultipart.newInstance();
        MimeBodyPart bodyPart1 = new MimeBodyPart(textBody, "text/plain");
        MimeBodyPart bodyPart2 = new MimeBodyPart(innerMessage, "message/rfc822");
        bodyPart2.setHeader("Content-Disposition", "inline; filename=\"message.eml\"");
        multipart.addBodyPart(bodyPart1);
        multipart.addBodyPart(bodyPart2);

        // Create message
        MimeMessage message = new MimeMessage();
        MimeMessageHelper.setBody(message, multipart);

        // Extract text
        List<Part> outputNonViewableParts = new ArrayList<Part>();
        ArrayList<Viewable> outputViewableParts = new ArrayList<>();
        MessageExtractor.findViewablesAndAttachments(message, outputViewableParts, outputNonViewableParts);
        ViewableExtractedText container = messageViewInfoExtractor.extractTextFromViewables(outputViewableParts);

        String expectedText =
                BODY_TEXT +
                "\r\n\r\n" +
                "----- message.eml ------------------------------------------------------" +
                "\r\n\r\n" +
                "From: from@example.com" + "\r\n" +
                "To: to@example.com" + "\r\n" +
                "Sent: Sat Mar 17 00:00:00 GMT+01:00 2012" + "\r\n" +
                "Subject: Subject" + "\r\n" +
                "\r\n" +
                innerBodyText;
        String expectedHtml =
                "<pre class=\"k9mail\">" +
                        BODY_TEXT_HTML +
                "</pre>" +
                "<p style=\"margin-top: 2.5em; margin-bottom: 1em; border-bottom: " +
                        "1px solid #000\">message.eml</p>" +
                "<table style=\"border: 0\">" +
                "<tr>" +
                "<th style=\"text-align: left; vertical-align: top;\">From:</th>" +
                "<td>from@example.com</td>" +
                "</tr><tr>" +
                "<th style=\"text-align: left; vertical-align: top;\">To:</th>" +
                "<td>to@example.com</td>" +
                "</tr><tr>" +
                "<th style=\"text-align: left; vertical-align: top;\">Sent:</th>" +
                "<td>Sat Mar 17 00:00:00 GMT+01:00 2012</td>" +
                "</tr><tr>" +
                "<th style=\"text-align: left; vertical-align: top;\">Subject:</th>" +
                "<td>Subject</td>" +
                "</tr>" +
                "</table>" +
                "<pre class=\"k9mail\">" +
                innerBodyText +
                "</pre>";

        assertEquals(expectedText, container.text);
        assertEquals(expectedHtml, getHtmlBodyText(container.html));
    }

    private static String getHtmlBodyText(String htmlText) {
        htmlText = htmlText.substring(htmlText.indexOf("<body>") +6);
        htmlText = htmlText.substring(0, htmlText.indexOf("</body>"));
        return htmlText;
    }

}
