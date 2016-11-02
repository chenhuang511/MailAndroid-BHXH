package vn.bhxh.bhxhmail.message.extractors;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import vn.bhxh.bhxhmail.mail.Message;
import vn.bhxh.bhxhmail.message.MessageCreationHelper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml", sdk = 21)
public class EncryptionDetectorTest {
    private static final String CRLF = "\r\n";


    private TextPartFinder textPartFinder;
    private EncryptionDetector encryptionDetector;


    @Before
    public void setUp() throws Exception {
        textPartFinder = mock(TextPartFinder.class);

        encryptionDetector = new EncryptionDetector(textPartFinder);
    }

    @Test
    public void isEncrypted_withTextPlain_shouldReturnFalse() throws Exception {
        Message message = MessageCreationHelper.createTextMessage("text/plain", "plain text");

        boolean encrypted = encryptionDetector.isEncrypted(message);

        assertFalse(encrypted);
    }

    @Test
    public void isEncrypted_withMultipartEncrypted_shouldReturnTrue() throws Exception {
        Message message = MessageCreationHelper.createMultipartMessage("multipart/encrypted",
                MessageCreationHelper.createPart("application/octet-stream"), MessageCreationHelper.createPart("application/octet-stream"));

        boolean encrypted = encryptionDetector.isEncrypted(message);

        assertTrue(encrypted);
    }

    @Test
    public void isEncrypted_withSMimePart_shouldReturnTrue() throws Exception {
        Message message = MessageCreationHelper.createMessage("application/pkcs7-mime");

        boolean encrypted = encryptionDetector.isEncrypted(message);

        assertTrue(encrypted);
    }

    @Test
    public void isEncrypted_withMultipartMixedContainingSMimePart_shouldReturnTrue() throws Exception {
        Message message = MessageCreationHelper.createMultipartMessage("multipart/mixed",
                MessageCreationHelper.createPart("application/pkcs7-mime"), MessageCreationHelper.createPart("text/plain"));

        boolean encrypted = encryptionDetector.isEncrypted(message);

        assertTrue(encrypted);
    }

    @Test
    public void isEncrypted_withInlinePgp_shouldReturnTrue() throws Exception {
        Message message = MessageCreationHelper.createTextMessage("text/plain", "" +
                "-----BEGIN PGP MESSAGE-----" + CRLF +
                "some encrypted stuff here" + CRLF +
                "-----END PGP MESSAGE-----");
        when(textPartFinder.findFirstTextPart(message)).thenReturn(message);

        boolean encrypted = encryptionDetector.isEncrypted(message);

        assertTrue(encrypted);
    }

    @Test
    public void isEncrypted_withPlainTextAndPreambleWithInlinePgp_shouldReturnFalse() throws Exception {
        Message message = MessageCreationHelper.createTextMessage("text/plain", "" +
                "preamble" + CRLF +
                "-----BEGIN PGP MESSAGE-----" + CRLF +
                "some encrypted stuff here" + CRLF +
                "-----END PGP MESSAGE-----" + CRLF +
                "epilogue");
        when(textPartFinder.findFirstTextPart(message)).thenReturn(message);

        boolean encrypted = encryptionDetector.isEncrypted(message);

        assertFalse(encrypted);
    }

    @Test
    public void isEncrypted_withQuotedInlinePgp_shouldReturnFalse() throws Exception {
        Message message = MessageCreationHelper.createTextMessage("text/plain", "" +
                "good talk!" + CRLF +
                CRLF +
                "> -----BEGIN PGP MESSAGE-----" + CRLF +
                "> some encrypted stuff here" + CRLF +
                "> -----END PGP MESSAGE-----" + CRLF +
                CRLF +
                "-- " + CRLF +
                "my signature");
        when(textPartFinder.findFirstTextPart(message)).thenReturn(message);

        boolean encrypted = encryptionDetector.isEncrypted(message);

        assertFalse(encrypted);
    }
}
