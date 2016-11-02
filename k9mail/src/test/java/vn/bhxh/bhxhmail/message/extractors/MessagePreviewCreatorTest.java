package vn.bhxh.bhxhmail.message.extractors;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import vn.bhxh.bhxhmail.mail.Message;
import vn.bhxh.bhxhmail.mail.Part;
import vn.bhxh.bhxhmail.mail.internet.MimeMessage;
import vn.bhxh.bhxhmail.message.MessageCreationHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


public class MessagePreviewCreatorTest {
    private TextPartFinder textPartFinder;
    private PreviewTextExtractor previewTextExtractor;
    private EncryptionDetector encryptionDetector;
    private MessagePreviewCreator previewCreator;

    @Before
    public void setUp() throws Exception {
        textPartFinder = mock(TextPartFinder.class);
        previewTextExtractor = mock(PreviewTextExtractor.class);
        encryptionDetector = mock(EncryptionDetector.class);

        previewCreator = new MessagePreviewCreator(textPartFinder, previewTextExtractor, encryptionDetector);
    }

    @Test
    public void createPreview_withEncryptedMessage() throws Exception {
        Message message = createDummyMessage();
        when(encryptionDetector.isEncrypted(message)).thenReturn(true);

        PreviewResult result = previewCreator.createPreview(message);

        assertFalse(result.isPreviewTextAvailable());
        Assert.assertEquals(PreviewResult.PreviewType.ENCRYPTED, result.getPreviewType());
        verifyNoMoreInteractions(textPartFinder);
        verifyNoMoreInteractions(previewTextExtractor);
    }

    @Test
    public void createPreview_withoutTextPart() throws Exception {
        Message message = createDummyMessage();
        when(encryptionDetector.isEncrypted(message)).thenReturn(false);
        when(textPartFinder.findFirstTextPart(message)).thenReturn(null);

        PreviewResult result = previewCreator.createPreview(message);

        assertFalse(result.isPreviewTextAvailable());
        Assert.assertEquals(PreviewResult.PreviewType.NONE, result.getPreviewType());
        verifyNoMoreInteractions(previewTextExtractor);
    }

    @Test
    public void createPreview_withEmptyTextPart() throws Exception {
        Message message = createDummyMessage();
        Part textPart = MessageCreationHelper.createEmptyPart("text/plain");
        when(encryptionDetector.isEncrypted(message)).thenReturn(false);
        when(textPartFinder.findFirstTextPart(message)).thenReturn(textPart);

        PreviewResult result = previewCreator.createPreview(message);

        assertFalse(result.isPreviewTextAvailable());
        Assert.assertEquals(PreviewResult.PreviewType.NONE, result.getPreviewType());
        verifyNoMoreInteractions(previewTextExtractor);
    }

    @Test
    public void createPreview_withTextPart() throws Exception {
        Message message = createDummyMessage();
        Part textPart = MessageCreationHelper.createTextPart("text/plain");
        when(encryptionDetector.isEncrypted(message)).thenReturn(false);
        when(textPartFinder.findFirstTextPart(message)).thenReturn(textPart);
        when(previewTextExtractor.extractPreview(textPart)).thenReturn("expected");

        PreviewResult result = previewCreator.createPreview(message);

        assertTrue(result.isPreviewTextAvailable());
        Assert.assertEquals(PreviewResult.PreviewType.TEXT, result.getPreviewType());
        assertEquals("expected", result.getPreviewText());
    }

    @Test
    public void createPreview_withPreviewTextExtractorThrowing() throws Exception {
        Message message = createDummyMessage();
        Part textPart = MessageCreationHelper.createTextPart("text/plain");
        when(encryptionDetector.isEncrypted(message)).thenReturn(false);
        when(textPartFinder.findFirstTextPart(message)).thenReturn(textPart);
        when(previewTextExtractor.extractPreview(textPart)).thenThrow(new PreviewExtractionException(""));

        PreviewResult result = previewCreator.createPreview(message);

        assertFalse(result.isPreviewTextAvailable());
        Assert.assertEquals(PreviewResult.PreviewType.ERROR, result.getPreviewType());
    }

    private Message createDummyMessage() {
        return new MimeMessage();
    }
}
