package vn.bhxh.bhxhmail.mailstore;


import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Map;

import vn.bhxh.bhxhmail.mail.BodyPart;
import vn.bhxh.bhxhmail.mail.Multipart;
import vn.bhxh.bhxhmail.mail.Part;
import vn.bhxh.bhxhmail.mail.internet.MimeBodyPart;
import vn.bhxh.bhxhmail.mail.internet.MimeHeader;
import vn.bhxh.bhxhmail.mail.internet.MimeMultipart;
import vn.bhxh.bhxhmail.message.extractors.AttachmentInfoExtractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SuppressWarnings("unchecked")
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class AttachmentResolverTest {
    public static final Uri ATTACHMENT_TEST_URI_1 = Uri.parse("uri://test/1");
    public static final Uri ATTACHMENT_TEST_URI_2 = Uri.parse("uri://test/2");


    private AttachmentInfoExtractor attachmentInfoExtractor;


    @Before
    public void setUp() throws Exception {
        attachmentInfoExtractor = mock(AttachmentInfoExtractor.class);
    }

    @Test
    public void buildCidMap__onPartWithNoBody__shouldReturnEmptyMap() throws Exception {
        Part part = new MimeBodyPart();

        Map<String,Uri> result = AttachmentResolver.buildCidToAttachmentUriMap(attachmentInfoExtractor, part);

        assertTrue(result.isEmpty());
    }

    @Test
    public void buildCidMap__onMultipartWithNoParts__shouldReturnEmptyMap() throws Exception {
        Multipart multipartBody = MimeMultipart.newInstance();
        Part multipartPart = new MimeBodyPart(multipartBody);

        Map<String,Uri> result = AttachmentResolver.buildCidToAttachmentUriMap(attachmentInfoExtractor, multipartPart);

        assertTrue(result.isEmpty());
    }

    @Test
    public void buildCidMap__onMultipartWithEmptyBodyPart__shouldReturnEmptyMap() throws Exception {
        Multipart multipartBody = MimeMultipart.newInstance();
        BodyPart bodyPart = spy(new MimeBodyPart());
        Part multipartPart = new MimeBodyPart(multipartBody);
        multipartBody.addBodyPart(bodyPart);

        Map<String,Uri> result = AttachmentResolver.buildCidToAttachmentUriMap(attachmentInfoExtractor, multipartPart);

        verify(bodyPart).getContentId();
        assertTrue(result.isEmpty());
    }

    @Test
    public void buildCidMap__onTwoPart__shouldReturnBothUris() throws Exception {
        Multipart multipartBody = MimeMultipart.newInstance();
        Part multipartPart = new MimeBodyPart(multipartBody);

        BodyPart subPart1 = new MimeBodyPart();
        BodyPart subPart2 = new MimeBodyPart();
        multipartBody.addBodyPart(subPart1);
        multipartBody.addBodyPart(subPart2);

        subPart1.setHeader(MimeHeader.HEADER_CONTENT_ID, "cid-1");
        subPart2.setHeader(MimeHeader.HEADER_CONTENT_ID, "cid-2");

        when(attachmentInfoExtractor.extractAttachmentInfo(subPart1)).thenReturn(new AttachmentViewInfo(
                        null, null, AttachmentViewInfo.UNKNOWN_SIZE, ATTACHMENT_TEST_URI_1, false, subPart1, true));
        when(attachmentInfoExtractor.extractAttachmentInfo(subPart2)).thenReturn(new AttachmentViewInfo(
                        null, null, AttachmentViewInfo.UNKNOWN_SIZE, ATTACHMENT_TEST_URI_2, false, subPart2, true));


        Map<String,Uri> result = AttachmentResolver.buildCidToAttachmentUriMap(attachmentInfoExtractor, multipartPart);


        assertEquals(2, result.size());
        assertEquals(ATTACHMENT_TEST_URI_1, result.get("cid-1"));
        assertEquals(ATTACHMENT_TEST_URI_2, result.get("cid-2"));
    }
}
