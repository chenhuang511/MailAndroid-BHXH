package vn.bhxh.bhxhmail.message.extractors;


import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import vn.bhxh.bhxhmail.mail.Part;
import vn.bhxh.bhxhmail.mail.internet.MimeBodyPart;
import vn.bhxh.bhxhmail.mail.internet.MimeHeader;
import vn.bhxh.bhxhmail.mail.internet.TextBody;
import vn.bhxh.bhxhmail.mailstore.AttachmentViewInfo;
import vn.bhxh.bhxhmail.mailstore.DeferredFileBody;
import vn.bhxh.bhxhmail.mailstore.LocalBodyPart;
import vn.bhxh.bhxhmail.provider.AttachmentProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class AttachmentInfoExtractorTest {
    public static final Uri TEST_URI = Uri.parse("uri://test");
    public static final String TEST_MIME_TYPE = "text/plain";
    public static final long TEST_SIZE = 123L;
    public static final String TEST_ACCOUNT_UUID = "uuid";
    public static final long TEST_ID = 234L;
    public static final String TEST_CONTENT_ID = "test-content-id";


    private AttachmentInfoExtractor attachmentInfoExtractor;
    private Context context;


    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;
        attachmentInfoExtractor = new AttachmentInfoExtractor(context);
    }

    @Test(expected = IllegalArgumentException.class)
    public void extractInfo__withGenericPart_shouldThrow() throws Exception {
        Part part = mock(Part.class);

        attachmentInfoExtractor.extractAttachmentInfo(part);
    }

    @Test
    public void extractInfo__fromLocalBodyPart__shouldReturnProvidedValues() throws Exception {
        LocalBodyPart part = new LocalBodyPart(TEST_ACCOUNT_UUID, null, TEST_ID, TEST_SIZE);
        part.setHeader(MimeHeader.HEADER_CONTENT_TYPE, TEST_MIME_TYPE);

        AttachmentViewInfo attachmentViewInfo = attachmentInfoExtractor.extractAttachmentInfo(part);

        assertEquals(AttachmentProvider.getAttachmentUri(TEST_ACCOUNT_UUID, TEST_ID), attachmentViewInfo.uri);
        assertEquals(TEST_SIZE, attachmentViewInfo.size);
        assertEquals(TEST_MIME_TYPE, attachmentViewInfo.mimeType);
    }

    @Test
    public void extractInfoForDb__withNoHeaders__shouldReturnEmptyValues() throws Exception {
        MimeBodyPart part = new MimeBodyPart();

        AttachmentViewInfo attachmentViewInfo = attachmentInfoExtractor.extractAttachmentInfoForDatabase(part);

        assertEquals(Uri.EMPTY, attachmentViewInfo.uri);
        assertEquals(AttachmentViewInfo.UNKNOWN_SIZE, attachmentViewInfo.size);
        assertEquals("noname.txt", attachmentViewInfo.displayName);
        assertEquals("text/plain", attachmentViewInfo.mimeType);
        assertFalse(attachmentViewInfo.inlineAttachment);
    }

    @Test
    public void extractInfoForDb__withTextMimeType__shouldReturnTxtExtension() throws Exception {
        MimeBodyPart part = new MimeBodyPart();
        part.setHeader(MimeHeader.HEADER_CONTENT_TYPE, "text/plain");

        AttachmentViewInfo attachmentViewInfo = attachmentInfoExtractor.extractAttachmentInfoForDatabase(part);

        // MimeUtility.getExtensionByMimeType("text/plain"); -> "txt"
        assertEquals("noname.txt", attachmentViewInfo.displayName);
        assertEquals("text/plain", attachmentViewInfo.mimeType);
    }

    @Test
    public void extractInfoForDb__withContentTypeAndName__shouldReturnNamedAttachment() throws Exception {
        MimeBodyPart part = new MimeBodyPart();
        part.setHeader(MimeHeader.HEADER_CONTENT_TYPE, TEST_MIME_TYPE + "; name=\"filename.ext\"");

        AttachmentViewInfo attachmentViewInfo = attachmentInfoExtractor.extractAttachmentInfoForDatabase(part);

        assertEquals(Uri.EMPTY, attachmentViewInfo.uri);
        assertEquals(TEST_MIME_TYPE, attachmentViewInfo.mimeType);
        assertEquals("filename.ext", attachmentViewInfo.displayName);
        assertFalse(attachmentViewInfo.inlineAttachment);
    }

    @Test
    public void extractInfoForDb__withContentTypeAndEncodedWordName__shouldReturnDecodedName() throws Exception {
        Part part = new MimeBodyPart();
        part.addRawHeader(MimeHeader.HEADER_CONTENT_TYPE,
                MimeHeader.HEADER_CONTENT_TYPE + ": " +TEST_MIME_TYPE + "; name=\"=?ISO-8859-1?Q?Sm=F8rrebr=F8d?=\"");

        AttachmentViewInfo attachmentViewInfo = attachmentInfoExtractor.extractAttachmentInfoForDatabase(part);

        assertEquals("Smørrebrød", attachmentViewInfo.displayName);
    }

    @Test
    public void extractInfoForDb__withDispositionAttach__shouldReturnNamedAttachment() throws Exception {
        MimeBodyPart part = new MimeBodyPart();
        part.setHeader(MimeHeader.HEADER_CONTENT_DISPOSITION,
                "attachment" + "; filename=\"filename.ext\"; meaningless=\"dummy\"");

        AttachmentViewInfo attachmentViewInfo = attachmentInfoExtractor.extractAttachmentInfoForDatabase(part);

        assertEquals(Uri.EMPTY, attachmentViewInfo.uri);
        assertEquals("filename.ext", attachmentViewInfo.displayName);
        assertFalse(attachmentViewInfo.inlineAttachment);
    }

    @Test
    public void extractInfoForDb__withDispositionInlineAndContentId__shouldReturnInlineAttachment()
            throws Exception {
        Part part = new MimeBodyPart();
        part.addRawHeader(MimeHeader.HEADER_CONTENT_ID, MimeHeader.HEADER_CONTENT_ID + ": " + TEST_CONTENT_ID);
        part.addRawHeader(MimeHeader.HEADER_CONTENT_DISPOSITION, MimeHeader.HEADER_CONTENT_DISPOSITION + ": " +
                "inline" + ";\n  filename=\"filename.ext\";\n  meaningless=\"dummy\"");

        AttachmentViewInfo attachmentViewInfo = attachmentInfoExtractor.extractAttachmentInfoForDatabase(part);

        assertTrue(attachmentViewInfo.inlineAttachment);
    }

    @Test
    public void extractInfoForDb__withDispositionSizeParam__shouldReturnThatSize() throws Exception {
        MimeBodyPart part = new MimeBodyPart();
        part.setHeader(MimeHeader.HEADER_CONTENT_DISPOSITION, "attachment" + "; size=\"" + TEST_SIZE + "\"");

        AttachmentViewInfo attachmentViewInfo = attachmentInfoExtractor.extractAttachmentInfoForDatabase(part);

        assertEquals(TEST_SIZE, attachmentViewInfo.size);
    }

    @Test
    public void extractInfoForDb__withDispositionInvalidSizeParam__shouldReturnUnknownSize() throws Exception {
        MimeBodyPart part = new MimeBodyPart();
        part.setHeader(MimeHeader.HEADER_CONTENT_DISPOSITION, "attachment" + "; size=\"notanint\"");

        AttachmentViewInfo attachmentViewInfo = attachmentInfoExtractor.extractAttachmentInfoForDatabase(part);

        assertEquals(AttachmentViewInfo.UNKNOWN_SIZE, attachmentViewInfo.size);
    }

    @Test
    public void extractInfoForDb__withNoBody__shouldReturnContentNotAvailable() throws Exception {
        MimeBodyPart part = new MimeBodyPart();

        AttachmentViewInfo attachmentViewInfo = attachmentInfoExtractor.extractAttachmentInfoForDatabase(part);

        assertFalse(attachmentViewInfo.isContentAvailable);
    }

    @Test
    public void extractInfoForDb__withNoBody__shouldReturnContentAvailable() throws Exception {
        MimeBodyPart part = new MimeBodyPart();
        part.setBody(new TextBody("data"));

        AttachmentViewInfo attachmentViewInfo = attachmentInfoExtractor.extractAttachmentInfoForDatabase(part);

        assertTrue(attachmentViewInfo.isContentAvailable);
    }

    @Test
    public void extractInfo__withDeferredFileBody() throws Exception {
        attachmentInfoExtractor = new AttachmentInfoExtractor(context) {
            @Nullable
            @Override
            protected Uri getDecryptedFileProviderUri(DeferredFileBody decryptedTempFileBody, String mimeType) {
                return TEST_URI;
            }
        };

        DeferredFileBody body = mock(DeferredFileBody.class);
        when(body.getSize()).thenReturn(TEST_SIZE);

        MimeBodyPart part = new MimeBodyPart();
        part.setBody(body);
        part.setHeader(MimeHeader.HEADER_CONTENT_TYPE, TEST_MIME_TYPE);


        AttachmentViewInfo attachmentViewInfo = attachmentInfoExtractor.extractAttachmentInfo(part);


        assertEquals(TEST_URI, attachmentViewInfo.uri);
        assertEquals(TEST_SIZE, attachmentViewInfo.size);
        assertEquals(TEST_MIME_TYPE, attachmentViewInfo.mimeType);
        assertFalse(attachmentViewInfo.inlineAttachment);
        assertTrue(attachmentViewInfo.isContentAvailable);
    }
}
