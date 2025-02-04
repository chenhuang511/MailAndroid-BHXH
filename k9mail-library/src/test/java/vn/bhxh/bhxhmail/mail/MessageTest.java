package vn.bhxh.bhxhmail.mail;


import android.content.Context;

import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.util.MimeUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.TimeZone;

import vn.bhxh.bhxhmail.mail.internet.BinaryTempFileBody;
import vn.bhxh.bhxhmail.mail.internet.BinaryTempFileMessageBody;
import vn.bhxh.bhxhmail.mail.internet.CharsetSupport;
import vn.bhxh.bhxhmail.mail.internet.MimeBodyPart;
import vn.bhxh.bhxhmail.mail.internet.MimeHeader;
import vn.bhxh.bhxhmail.mail.internet.MimeMessage;
import vn.bhxh.bhxhmail.mail.internet.MimeMessageHelper;
import vn.bhxh.bhxhmail.mail.internet.MimeMultipart;
import vn.bhxh.bhxhmail.mail.internet.TextBody;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class MessageTest {

    private Context context;

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;

        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
        BinaryTempFileBody.setTempDirectory(context.getCacheDir());
    }

    private static final String SEVEN_BIT_RESULT =
              "From: from@example.com\r\n"
            + "To: to@example.com\r\n"
            + "Subject: Test Message\r\n"
            + "Date: Wed, 28 Aug 2013 08:51:09 -0400\r\n"
            + "MIME-Version: 1.0\r\n"
            + "Content-Type: multipart/mixed; boundary=\"----Boundary103\"\r\n"
            + "Content-Transfer-Encoding: 7bit\r\n"
            + "\r\n"
            + "------Boundary103\r\n"
            + "Content-Transfer-Encoding: quoted-printable\r\n"
            + "Content-Type: text/plain;\r\n"
            + " charset=utf-8\r\n"
            + "\r\n"
            + "Testing=2E\r\n"
            + "This is a text body with some greek characters=2E\r\n"
            + "=CE=B1=CE=B2=CE=B3=CE=B4=CE=B5=CE=B6=CE=B7=CE=B8\r\n"
            + "End of test=2E\r\n"
            + "\r\n"
            + "------Boundary103\r\n"
            + "Content-Type: application/octet-stream\r\n"
            + "Content-Transfer-Encoding: base64\r\n"
            + "\r\n"
            + "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/\r\n"
            + "\r\n"
            + "------Boundary103\r\n"
            + "Content-Type: message/rfc822\r\n"
            + "Content-Disposition: attachment\r\n"
            + "Content-Transfer-Encoding: 7bit\r\n"
            + "\r\n"
            + "From: from@example.com\r\n"
            + "To: to@example.com\r\n"
            + "Subject: Test Message\r\n"
            + "Date: Wed, 28 Aug 2013 08:51:09 -0400\r\n"
            + "MIME-Version: 1.0\r\n"
            + "Content-Type: multipart/mixed; boundary=\"----Boundary102\"\r\n"
            + "Content-Transfer-Encoding: 7bit\r\n"
            + "\r\n"
            + "------Boundary102\r\n"
            + "Content-Transfer-Encoding: quoted-printable\r\n"
            + "Content-Type: text/plain;\r\n"
            + " charset=utf-8\r\n"
            + "\r\n"
            + "Testing=2E\r\n"
            + "This is a text body with some greek characters=2E\r\n"
            + "=CE=B1=CE=B2=CE=B3=CE=B4=CE=B5=CE=B6=CE=B7=CE=B8\r\n"
            + "End of test=2E\r\n"
            + "\r\n"
            + "------Boundary102\r\n"
            + "Content-Type: application/octet-stream\r\n"
            + "Content-Transfer-Encoding: base64\r\n"
            + "\r\n"
            + "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/\r\n"
            + "\r\n"
            + "------Boundary102\r\n"
            + "Content-Type: message/rfc822\r\n"
            + "Content-Disposition: attachment\r\n"
            + "Content-Transfer-Encoding: 7bit\r\n"
            + "\r\n"
            + "From: from@example.com\r\n"
            + "To: to@example.com\r\n"
            + "Subject: Test Message\r\n"
            + "Date: Wed, 28 Aug 2013 08:51:09 -0400\r\n"
            + "MIME-Version: 1.0\r\n"
            + "Content-Type: multipart/mixed; boundary=\"----Boundary101\"\r\n"
            + "Content-Transfer-Encoding: 7bit\r\n"
            + "\r\n"
            + "------Boundary101\r\n"
            + "Content-Transfer-Encoding: quoted-printable\r\n"
            + "Content-Type: text/plain;\r\n"
            + " charset=utf-8\r\n"
            + "\r\n"
            + "Testing=2E\r\n"
            + "This is a text body with some greek characters=2E\r\n"
            + "=CE=B1=CE=B2=CE=B3=CE=B4=CE=B5=CE=B6=CE=B7=CE=B8\r\n"
            + "End of test=2E\r\n"
            + "\r\n"
            + "------Boundary101\r\n"
            + "Content-Type: application/octet-stream\r\n"
            + "Content-Transfer-Encoding: base64\r\n"
            + "\r\n"
            + "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/\r\n"
            + "\r\n"
            + "------Boundary101--\r\n"
            + "\r\n"
            + "------Boundary102--\r\n"
            + "\r\n"
            + "------Boundary103--\r\n";

    private static final String TO_BODY_PART_RESULT =
                    "Content-Type: multipart/mixed; boundary=\"----Boundary103\"\r\n"
                    + "Content-Transfer-Encoding: 7bit\r\n"
                    + "\r\n"
                    + "------Boundary103\r\n"
                    + "Content-Transfer-Encoding: quoted-printable\r\n"
                    + "Content-Type: text/plain;\r\n"
                    + " charset=utf-8\r\n"
                    + "\r\n"
                    + "Testing=2E\r\n"
                    + "This is a text body with some greek characters=2E\r\n"
                    + "=CE=B1=CE=B2=CE=B3=CE=B4=CE=B5=CE=B6=CE=B7=CE=B8\r\n"
                    + "End of test=2E\r\n"
                    + "\r\n"
                    + "------Boundary103\r\n"
                    + "Content-Type: application/octet-stream\r\n"
                    + "Content-Transfer-Encoding: base64\r\n"
                    + "\r\n"
                    + "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/\r\n"
                    + "\r\n"
                    + "------Boundary103\r\n"
                    + "Content-Type: message/rfc822\r\n"
                    + "Content-Disposition: attachment\r\n"
                    + "Content-Transfer-Encoding: 7bit\r\n"
                    + "\r\n"
                    + "From: from@example.com\r\n"
                    + "To: to@example.com\r\n"
                    + "Subject: Test Message\r\n"
                    + "Date: Wed, 28 Aug 2013 08:51:09 -0400\r\n"
                    + "MIME-Version: 1.0\r\n"
                    + "Content-Type: multipart/mixed; boundary=\"----Boundary102\"\r\n"
                    + "Content-Transfer-Encoding: 7bit\r\n"
                    + "\r\n"
                    + "------Boundary102\r\n"
                    + "Content-Transfer-Encoding: quoted-printable\r\n"
                    + "Content-Type: text/plain;\r\n"
                    + " charset=utf-8\r\n"
                    + "\r\n"
                    + "Testing=2E\r\n"
                    + "This is a text body with some greek characters=2E\r\n"
                    + "=CE=B1=CE=B2=CE=B3=CE=B4=CE=B5=CE=B6=CE=B7=CE=B8\r\n"
                    + "End of test=2E\r\n"
                    + "\r\n"
                    + "------Boundary102\r\n"
                    + "Content-Type: application/octet-stream\r\n"
                    + "Content-Transfer-Encoding: base64\r\n"
                    + "\r\n"
                    + "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/\r\n"
                    + "\r\n"
                    + "------Boundary102\r\n"
                    + "Content-Type: message/rfc822\r\n"
                    + "Content-Disposition: attachment\r\n"
                    + "Content-Transfer-Encoding: 7bit\r\n"
                    + "\r\n"
                    + "From: from@example.com\r\n"
                    + "To: to@example.com\r\n"
                    + "Subject: Test Message\r\n"
                    + "Date: Wed, 28 Aug 2013 08:51:09 -0400\r\n"
                    + "MIME-Version: 1.0\r\n"
                    + "Content-Type: multipart/mixed; boundary=\"----Boundary101\"\r\n"
                    + "Content-Transfer-Encoding: 7bit\r\n"
                    + "\r\n"
                    + "------Boundary101\r\n"
                    + "Content-Transfer-Encoding: quoted-printable\r\n"
                    + "Content-Type: text/plain;\r\n"
                    + " charset=utf-8\r\n"
                    + "\r\n"
                    + "Testing=2E\r\n"
                    + "This is a text body with some greek characters=2E\r\n"
                    + "=CE=B1=CE=B2=CE=B3=CE=B4=CE=B5=CE=B6=CE=B7=CE=B8\r\n"
                    + "End of test=2E\r\n"
                    + "\r\n"
                    + "------Boundary101\r\n"
                    + "Content-Type: application/octet-stream\r\n"
                    + "Content-Transfer-Encoding: base64\r\n"
                    + "\r\n"
                    + "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/\r\n"
                    + "\r\n"
                    + "------Boundary101--\r\n"
                    + "\r\n"
                    + "------Boundary102--\r\n"
                    + "\r\n"
                    + "------Boundary103--\r\n";

    private int mMimeBoundary;

    @Test
    public void testSetSendDateSetsSentDate() throws Exception {
        Message message = sampleMessage();
        final int milliseconds = 0;
        Date date = new Date(milliseconds);
        message.setSentDate(date, false);
        Date sentDate = message.getSentDate();
        assertNotNull(sentDate);
        assertEquals(milliseconds, sentDate.getTime());
    }

    @Test
    public void testSetSendDateFormatsHeaderCorrectlyWithCurrentTimeZone() throws Exception {
        Message message = sampleMessage();
        message.setSentDate(new Date(0), false);
        assertEquals("Thu, 01 Jan 1970 09:00:00 +0900", message.getHeader("Date")[0]);
    }

    @Test
    public void testSetSendDateFormatsHeaderCorrectlyWithoutTimeZone() throws Exception {
        Message message = sampleMessage();
        message.setSentDate(new Date(0), true);
        assertEquals("Thu, 01 Jan 1970 00:00:00 +0000", message.getHeader("Date")[0]);
    }

    @Test
    public void testMessage() throws MessagingException, IOException {
        MimeMessage message;
        ByteArrayOutputStream out;

        BinaryTempFileBody.setTempDirectory(context.getCacheDir());

        mMimeBoundary = 101;
        message = nestedMessage(nestedMessage(sampleMessage()));
        out = new ByteArrayOutputStream();
        message.writeTo(out);
        assertEquals(SEVEN_BIT_RESULT, out.toString());
    }

    private MimeMessage nestedMessage(MimeMessage subMessage)
            throws MessagingException, IOException {
        BinaryTempFileMessageBody tempMessageBody = new BinaryTempFileMessageBody(MimeUtil.ENC_8BIT);

        OutputStream out = tempMessageBody.getOutputStream();
        try {
            subMessage.writeTo(out);
        } finally {
            out.close();
        }

        MimeBodyPart bodyPart = new MimeBodyPart(tempMessageBody, "message/rfc822");
        bodyPart.setHeader(MimeHeader.HEADER_CONTENT_DISPOSITION, "attachment");
        bodyPart.setEncoding(MimeUtil.ENC_7BIT);

        MimeMessage parentMessage = sampleMessage();
        ((Multipart) parentMessage.getBody()).addBodyPart(bodyPart);

        return parentMessage;
    }

    private MimeMessage sampleMessage() throws MessagingException, IOException {
        MimeMessage message = new MimeMessage();
        message.setFrom(new Address("from@example.com"));
        message.setRecipient(Message.RecipientType.TO, new Address("to@example.com"));
        message.setSubject("Test Message");
        message.setHeader("Date", "Wed, 28 Aug 2013 08:51:09 -0400");
        message.setEncoding(MimeUtil.ENC_7BIT);

        MimeMultipart multipartBody = new MimeMultipart("multipart/mixed", generateBoundary());
        multipartBody.addBodyPart(textBodyPart());
        multipartBody.addBodyPart(binaryBodyPart());
        MimeMessageHelper.setBody(message, multipartBody);

        return message;
    }

    private MimeBodyPart binaryBodyPart() throws IOException,
            MessagingException {
        String encodedTestString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvwxyz0123456789+/\r\n";

        BinaryTempFileBody tempFileBody = new BinaryTempFileBody(MimeUtil.ENC_BASE64);

        InputStream in = new ByteArrayInputStream(
                encodedTestString.getBytes("UTF-8"));

        OutputStream out = tempFileBody.getOutputStream();
        try {
            IOUtils.copy(in, out);
        } finally {
            out.close();
        }

        MimeBodyPart bodyPart = new MimeBodyPart(tempFileBody,
                "application/octet-stream");
        bodyPart.setEncoding(MimeUtil.ENC_BASE64);

        return bodyPart;
    }

    private MimeBodyPart textBodyPart() throws MessagingException {
        TextBody textBody = new TextBody(
                  "Testing.\r\n"
                + "This is a text body with some greek characters.\r\n"
                + "αβγδεζηθ\r\n"
                + "End of test.\r\n");
        textBody.setCharset("utf-8");

        MimeBodyPart bodyPart = new MimeBodyPart();
        MimeMessageHelper.setBody(bodyPart, textBody);
        CharsetSupport.setCharset("utf-8", bodyPart);
        return bodyPart;
    }

    private String generateBoundary() {
        return "----Boundary" + Integer.toString(mMimeBoundary++);
    }

    @Test
    public void testToBodyPart() throws MessagingException, IOException {
        MimeMessage message;
        ByteArrayOutputStream out;

        BinaryTempFileBody.setTempDirectory(context.getCacheDir());

        mMimeBoundary = 101;
        message = nestedMessage(nestedMessage(sampleMessage()));
        out = new ByteArrayOutputStream();
        MimeBodyPart bodyPart = message.toBodyPart();
        bodyPart.writeTo(out);
        assertEquals(TO_BODY_PART_RESULT, out.toString());
    }
}
