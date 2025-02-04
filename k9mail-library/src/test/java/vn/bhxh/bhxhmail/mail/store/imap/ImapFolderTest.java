package vn.bhxh.bhxhmail.mail.store.imap;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import vn.bhxh.bhxhmail.mail.FetchProfile;
import vn.bhxh.bhxhmail.mail.Flag;
import vn.bhxh.bhxhmail.mail.Folder;
import vn.bhxh.bhxhmail.mail.Message;
import vn.bhxh.bhxhmail.mail.MessageRetrievalListener;
import vn.bhxh.bhxhmail.mail.MessagingException;
import vn.bhxh.bhxhmail.mail.Part;
import vn.bhxh.bhxhmail.mail.store.StoreConfig;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.util.collections.Sets.newSet;


//TODO: Increase test coverage e.g. for fetch() and fetchPart()
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class ImapFolderTest {
    private ImapStore imapStore;
    private ImapConnection imapConnection;
    private StoreConfig storeConfig;


    @Before
    public void setUp() throws Exception {
        imapStore = mock(ImapStore.class);
        storeConfig = mock(StoreConfig.class);
        when(storeConfig.getInboxFolderName()).thenReturn("INBOX");
        when(imapStore.getCombinedPrefix()).thenReturn("");
        when(imapStore.getStoreConfig()).thenReturn(storeConfig);

        imapConnection = mock(ImapConnection.class);
    }

    @Test
    public void open_readWrite_shouldOpenFolder() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);

        imapFolder.open(Folder.OPEN_MODE_RW);

        assertTrue(imapFolder.isOpen());
    }

    @Test
    public void open_readOnly_shouldOpenFolder() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RO);

        imapFolder.open(Folder.OPEN_MODE_RO);

        assertTrue(imapFolder.isOpen());
    }

    @Test
    public void open_shouldFetchMessageCount() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);

        imapFolder.open(Folder.OPEN_MODE_RW);

        assertEquals(23, imapFolder.getMessageCount());
    }

    @Test
    public void open_readWrite_shouldMakeGetModeReturnReadWrite() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);

        imapFolder.open(Folder.OPEN_MODE_RW);

        Assert.assertEquals(Folder.OPEN_MODE_RW, imapFolder.getMode());
    }

    @Test
    public void open_readOnly_shouldMakeGetModeReturnReadOnly() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RO);

        imapFolder.open(Folder.OPEN_MODE_RO);

        Assert.assertEquals(Folder.OPEN_MODE_RO, imapFolder.getMode());
    }

    @Test
    public void open_shouldMakeExistReturnTrueWithoutExecutingAdditionalCommands() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);

        imapFolder.open(Folder.OPEN_MODE_RW);

        assertTrue(imapFolder.exists());
        verify(imapConnection, times(1)).executeSimpleCommand(anyString());
    }

    @Test
    public void open_calledTwice_shouldReuseSameImapConnection() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);
        imapFolder.open(Folder.OPEN_MODE_RW);

        imapFolder.open(Folder.OPEN_MODE_RW);

        verify(imapStore, times(1)).getConnection();
    }

    @Test
    public void open_withConnectionThrowingOnReUse_shouldCreateNewImapConnection() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);
        imapFolder.open(Folder.OPEN_MODE_RW);

        doThrow(IOException.class).when(imapConnection).executeSimpleCommand(Commands.NOOP);
        imapFolder.open(Folder.OPEN_MODE_RW);

        verify(imapStore, times(2)).getConnection();
    }

    @Test
    public void open_withIoException_shouldThrowMessagingException() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);
        doThrow(IOException.class).when(imapConnection).executeSimpleCommand("SELECT \"Folder\"");

        try {
            imapFolder.open(Folder.OPEN_MODE_RW);
            fail("Expected exception");
        } catch (MessagingException e) {
            assertNotNull(e.getCause());
            assertEquals(IOException.class, e.getCause().getClass());
        }
    }

    @Test
    public void open_withMessagingException_shouldThrowMessagingException() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);
        doThrow(MessagingException.class).when(imapConnection).executeSimpleCommand("SELECT \"Folder\"");

        try {
            imapFolder.open(Folder.OPEN_MODE_RW);
            fail("Expected exception");
        } catch (MessagingException ignored) {
        }
    }

    @Test
    public void open_withoutExistsResponse_shouldThrowMessagingException() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);
        List<ImapResponse> selectResponses = asList(
                ImapResponseHelper.createImapResponse("* OK [UIDNEXT 57576] Predicted next UID"),
                ImapResponseHelper.createImapResponse("2 OK [READ-WRITE] Select completed.")
        );
        when(imapConnection.executeSimpleCommand("SELECT \"Folder\"")).thenReturn(selectResponses);

        try {
            imapFolder.open(Folder.OPEN_MODE_RW);
            fail("Expected exception");
        } catch (MessagingException e) {
            assertEquals("Did not find message count during open", e.getMessage());
        }
    }

    @Test
    public void close_shouldCloseImapFolder() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);
        imapFolder.open(Folder.OPEN_MODE_RW);

        imapFolder.close();

        assertFalse(imapFolder.isOpen());
    }

    @Test
    public void exists_withClosedFolder_shouldOpenConnectionAndIssueStatusCommand() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);

        imapFolder.exists();

        verify(imapConnection).executeSimpleCommand("STATUS \"Folder\" (UIDVALIDITY)");
    }

    @Test
    public void exists_withoutNegativeImapResponse_shouldReturnTrue() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);

        boolean result = imapFolder.exists();

        assertTrue(result);
    }

    @Test
    public void exists_withNegativeImapResponse_shouldReturnFalse() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);
        Mockito.doThrow(NegativeImapResponseException.class).when(imapConnection)
                .executeSimpleCommand("STATUS \"Folder\" (UIDVALIDITY)");

        boolean result = imapFolder.exists();

        assertFalse(result);
    }

    @Test
    public void create_withClosedFolder_shouldOpenConnectionAndIssueCreateCommand() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);

        imapFolder.create(Folder.FolderType.HOLDS_MESSAGES);

        verify(imapConnection).executeSimpleCommand("CREATE \"Folder\"");
    }

    @Test
    public void create_withoutNegativeImapResponse_shouldReturnTrue() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);

        boolean result = imapFolder.create(Folder.FolderType.HOLDS_MESSAGES);

        assertTrue(result);
    }

    @Test
    public void create_withNegativeImapResponse_shouldReturnFalse() throws Exception {
        ImapFolder imapFolder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);
        Mockito.doThrow(NegativeImapResponseException.class).when(imapConnection).executeSimpleCommand("CREATE \"Folder\"");

        boolean result = imapFolder.create(Folder.FolderType.HOLDS_MESSAGES);

        assertFalse(result);
    }

    @Test
    public void copyMessages_withoutDestinationFolderOfWrongType_shouldThrow() throws Exception {
        ImapFolder sourceFolder = createFolder("Source");
        Folder destinationFolder = mock(Folder.class);
        List<ImapMessage> messages = singletonList(mock(ImapMessage.class));

        try {
            sourceFolder.copyMessages(messages, destinationFolder);
            fail("Expected exception");
        } catch (MessagingException e) {
            assertEquals("ImapFolder.copyMessages passed non-ImapFolder", e.getMessage());
        }
    }

    @Test
    public void copyMessages_withEmptyMessageList_shouldReturnNull() throws Exception {
        ImapFolder sourceFolder = createFolder("Source");
        ImapFolder destinationFolder = createFolder("Destination");
        List<ImapMessage> messages = Collections.emptyList();

        Map<String, String> result = sourceFolder.copyMessages(messages, destinationFolder);

        assertNull(result);
    }

    @Test
    public void copyMessages_withClosedFolder_shouldThrow() throws Exception {
        ImapFolder sourceFolder = createFolder("Source");
        ImapFolder destinationFolder = createFolder("Destination");
        when(imapStore.getConnection()).thenReturn(imapConnection);
        when(imapStore.getCombinedPrefix()).thenReturn("");
        List<ImapMessage> messages = singletonList(mock(ImapMessage.class));

        try {
            sourceFolder.copyMessages(messages, destinationFolder);
            fail("Expected exception");
        } catch (MessagingException e) {
            assertEquals("Folder Source is not open.", e.getMessage());
        }
    }

    @Test
    public void copyMessages() throws Exception {
        ImapFolder sourceFolder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);
        ImapFolder destinationFolder = createFolder("Destination");
        List<ImapMessage> messages = singletonList(createImapMessage("1"));
        List<ImapResponse> copyResponses = singletonList(
                ImapResponseHelper.createImapResponse("x OK [COPYUID 23 1 101] Success")
        );
        when(imapConnection.executeSimpleCommand("UID COPY 1 \"Destination\"")).thenReturn(copyResponses);
        sourceFolder.open(Folder.OPEN_MODE_RW);

        Map<String, String> result = sourceFolder.copyMessages(messages, destinationFolder);

        assertNotNull(result);
        assertEquals("101", result.get("1"));
    }

    @Test
    public void moveMessages_shouldCopyMessages() throws Exception {
        ImapFolder sourceFolder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);
        ImapFolder destinationFolder = createFolder("Destination");
        List<ImapMessage> messages = singletonList(createImapMessage("1"));
        List<ImapResponse> copyResponses = singletonList(
                ImapResponseHelper.createImapResponse("x OK [COPYUID 23 1 101] Success")
        );
        when(imapConnection.executeSimpleCommand("UID COPY 1 \"Destination\"")).thenReturn(copyResponses);
        sourceFolder.open(Folder.OPEN_MODE_RW);

        Map<String, String> result = sourceFolder.moveMessages(messages, destinationFolder);

        assertNotNull(result);
        assertEquals("101", result.get("1"));
    }

    @Test
    public void moveMessages_shouldDeleteMessagesFromSourceFolder() throws Exception {
        ImapFolder sourceFolder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);
        ImapFolder destinationFolder = createFolder("Destination");
        List<ImapMessage> messages = singletonList(createImapMessage("1"));
        List<ImapResponse> copyResponses = singletonList(
                ImapResponseHelper.createImapResponse("x OK [COPYUID 23 1 101] Success")
        );
        when(imapConnection.executeSimpleCommand("UID COPY 1 \"Destination\"")).thenReturn(copyResponses);
        sourceFolder.open(Folder.OPEN_MODE_RW);

        sourceFolder.moveMessages(messages, destinationFolder);

        verify(imapConnection).executeSimpleCommand("UID STORE 1 +FLAGS.SILENT (\\Deleted)");
    }

    @Test
    public void moveMessages_withEmptyMessageList_shouldReturnNull() throws Exception {
        ImapFolder sourceFolder = createFolder("Source");
        ImapFolder destinationFolder = createFolder("Destination");
        List<ImapMessage> messages = Collections.emptyList();

        Map<String, String> result = sourceFolder.moveMessages(messages, destinationFolder);

        assertNull(result);
    }

    @Test
    public void delete_withEmptyMessageList_shouldNotInteractWithImapConnection() throws Exception {
        ImapFolder folder = createFolder("Source");
        List<ImapMessage> messages = Collections.emptyList();

        folder.delete(messages, "Trash");

        verifyNoMoreInteractions(imapConnection);
    }

    @Test
    public void delete_fromTrashFolder_shouldIssueUidStoreFlagsCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);
        List<ImapMessage> messages = singletonList(createImapMessage("23"));
        folder.open(Folder.OPEN_MODE_RW);

        folder.delete(messages, "Folder");

        verify(imapConnection).executeSimpleCommand("UID STORE 23 +FLAGS.SILENT (\\Deleted)");
    }

    @Test
    public void delete_shouldMoveMessagesToTrashFolder() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);
        ImapFolder trashFolder = createFolder("Trash");
        when(imapStore.getFolder("Trash")).thenReturn(trashFolder);
        List<ImapMessage> messages = singletonList(createImapMessage("2"));
        List<ImapResponse> copyResponses = singletonList(
                ImapResponseHelper.createImapResponse("x OK [COPYUID 23 2 102] Success")
        );
        when(imapConnection.executeSimpleCommand("UID COPY 2 \"Trash\"")).thenReturn(copyResponses);
        folder.open(Folder.OPEN_MODE_RW);

        folder.delete(messages, "Trash");

        verify(imapConnection).executeSimpleCommand("UID STORE 2 +FLAGS.SILENT (\\Deleted)");
    }

    @Test
    public void delete_withoutTrashFolderExisting_shouldCreateTrashFolder() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);
        ImapFolder trashFolder = createFolder("Trash");
        when(imapStore.getFolder("Trash")).thenReturn(trashFolder);
        List<ImapMessage> messages = singletonList(createImapMessage("2"));
        List<ImapResponse> copyResponses = singletonList(
                ImapResponseHelper.createImapResponse("x OK [COPYUID 23 2 102] Success")
        );
        when(imapConnection.executeSimpleCommand("UID COPY 2 \"Trash\"")).thenReturn(copyResponses);
        folder.open(Folder.OPEN_MODE_RW);
        Mockito.doThrow(NegativeImapResponseException.class).doReturn(Collections.emptyList())
                .when(imapConnection).executeSimpleCommand("STATUS \"Trash\" (RECENT)");

        folder.delete(messages, "Trash");

        verify(imapConnection).executeSimpleCommand("CREATE \"Trash\"");
    }

    @Test
    public void getUnreadMessageCount_withClosedFolder_shouldThrow() throws Exception {
        ImapFolder folder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);

        try {
            folder.getUnreadMessageCount();
            fail("Expected exception");
        } catch (MessagingException e) {
            assertCheckOpenErrorMessage("Folder", e);
        }
    }

    @Test
    public void getUnreadMessageCount() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);
        List<ImapResponse> imapResponses = singletonList(ImapResponseHelper.createImapResponse("* SEARCH 1 2 3"));
        when(imapConnection.executeSimpleCommand("SEARCH 1:* UNSEEN NOT DELETED")).thenReturn(imapResponses);
        folder.open(Folder.OPEN_MODE_RW);

        int result = folder.getUnreadMessageCount();

        assertEquals(3, result);
    }

    @Test
    public void getFlaggedMessageCount_withClosedFolder_shouldThrow() throws Exception {
        ImapFolder folder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);

        try {
            folder.getFlaggedMessageCount();
            fail("Expected exception");
        } catch (MessagingException e) {
            assertCheckOpenErrorMessage("Folder", e);
        }
    }

    @Test
    public void getFlaggedMessageCount() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);
        List<ImapResponse> imapResponses = asList(
                ImapResponseHelper.createImapResponse("* SEARCH 1 2"),
                ImapResponseHelper.createImapResponse("* SEARCH 23 42")
        );
        when(imapConnection.executeSimpleCommand("SEARCH 1:* FLAGGED NOT DELETED")).thenReturn(imapResponses);
        folder.open(Folder.OPEN_MODE_RW);

        int result = folder.getFlaggedMessageCount();

        assertEquals(4, result);
    }

    @Test
    public void getHighestUid() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);
        List<ImapResponse> imapResponses = singletonList(ImapResponseHelper.createImapResponse("* SEARCH 42"));
        when(imapConnection.executeSimpleCommand("UID SEARCH *:*")).thenReturn(imapResponses);
        folder.open(Folder.OPEN_MODE_RW);

        long result = folder.getHighestUid();

        assertEquals(42L, result);
    }

    @Test
    public void getMessages_withoutDateConstraint() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);
        List<ImapResponse> imapResponses = asList(
                ImapResponseHelper.createImapResponse("* SEARCH 3"),
                ImapResponseHelper.createImapResponse("* SEARCH 5"),
                ImapResponseHelper.createImapResponse("* SEARCH 6")
        );
        when(imapConnection.executeSimpleCommand("UID SEARCH 1:10 NOT DELETED")).thenReturn(imapResponses);
        folder.open(Folder.OPEN_MODE_RW);

        List<ImapMessage> messages = folder.getMessages(1, 10, null, null);

        assertNotNull(messages);
        assertEquals(newSet("3", "5", "6"), extractMessageUids(messages));
    }

    @Test
    public void getMessages_withDateConstraint() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);
        List<ImapResponse> imapResponses = asList(
                ImapResponseHelper.createImapResponse("* SEARCH 47"),
                ImapResponseHelper.createImapResponse("* SEARCH 18")
        );
        when(imapConnection.executeSimpleCommand("UID SEARCH 1:10 SINCE 06-Feb-2016 NOT DELETED")).thenReturn(imapResponses);
        folder.open(Folder.OPEN_MODE_RW);

        List<ImapMessage> messages = folder.getMessages(1, 10, new Date(1454719826000L), null);

        assertNotNull(messages);
        assertEquals(newSet("18", "47"), extractMessageUids(messages));
    }

    @Test
    public void getMessages_withListener_shouldCallListener() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);
        List<ImapResponse> imapResponses = singletonList(ImapResponseHelper.createImapResponse("* SEARCH 99"));
        when(imapConnection.executeSimpleCommand("UID SEARCH 1:10 NOT DELETED")).thenReturn(imapResponses);
        folder.open(Folder.OPEN_MODE_RW);
        MessageRetrievalListener<ImapMessage> listener = createMessageRetrievalListener();

        List<ImapMessage> result = folder.getMessages(1, 10, null, listener);

        ImapMessage message = result.get(0);
        verify(listener).messageStarted("99", 0, 1);
        verify(listener).messageFinished(message, 0, 1);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void getMessages_withInvalidStartArgument_shouldThrow() throws Exception {
        ImapFolder folder = createFolder("Folder");

        try {
            folder.getMessages(0, 10, null, null);
            fail("Expected exception");
        } catch (MessagingException e) {
            assertEquals("Invalid message set 0 10", e.getMessage());
        }
    }

    @Test
    public void getMessages_withInvalidEndArgument_shouldThrow() throws Exception {
        ImapFolder folder = createFolder("Folder");

        try {
            folder.getMessages(10, 0, null, null);
            fail("Expected exception");
        } catch (MessagingException e) {
            assertEquals("Invalid message set 10 0", e.getMessage());
        }
    }

    @Test
    public void getMessages_withEndArgumentSmallerThanStartArgument_shouldThrow() throws Exception {
        ImapFolder folder = createFolder("Folder");

        try {
            folder.getMessages(10, 5, null, null);
            fail("Expected exception");
        } catch (MessagingException e) {
            assertEquals("Invalid message set 10 5", e.getMessage());
        }
    }

    @Test
    public void getMessages_withClosedFolder_shouldThrow() throws Exception {
        ImapFolder folder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);

        try {
            folder.getMessages(1, 5, null, null);
            fail("Expected exception");
        } catch (MessagingException e) {
            assertCheckOpenErrorMessage("Folder", e);
        }
    }

    @Test
    public void getMessages_sequenceNumbers_withClosedFolder_shouldThrow() throws Exception {
        ImapFolder folder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);

        try {
            folder.getMessages(asList(1L, 2L, 5L), false, null);
            fail("Expected exception");
        } catch (MessagingException e) {
            assertCheckOpenErrorMessage("Folder", e);
        }
    }

    @Test
    public void getMessages_sequenceNumbers() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);
        List<ImapResponse> imapResponses = asList(
                ImapResponseHelper.createImapResponse("* SEARCH 17"),
                ImapResponseHelper.createImapResponse("* SEARCH 18"),
                ImapResponseHelper.createImapResponse("* SEARCH 49")
        );
        when(imapConnection.executeSimpleCommand("UID SEARCH 1,2,5 NOT DELETED")).thenReturn(imapResponses);
        folder.open(Folder.OPEN_MODE_RW);

        List<ImapMessage> messages = folder.getMessages(asList(1L, 2L, 5L), false, null);

        assertNotNull(messages);
        assertEquals(newSet("17", "18", "49"), extractMessageUids(messages));
    }

    @Test
    public void getMessages_sequenceNumbers_withListener_shouldCallListener() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);
        List<ImapResponse> imapResponses = singletonList(ImapResponseHelper.createImapResponse("* SEARCH 99"));
        when(imapConnection.executeSimpleCommand("UID SEARCH 1")).thenReturn(imapResponses);
        folder.open(Folder.OPEN_MODE_RW);
        MessageRetrievalListener<ImapMessage> listener = createMessageRetrievalListener();

        List<ImapMessage> result = folder.getMessages(singletonList(1L), true, listener);

        ImapMessage message = result.get(0);
        verify(listener).messageStarted("99", 0, 1);
        verify(listener).messageFinished(message, 0, 1);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void getMessagesFromUids_withClosedFolder_shouldThrow() throws Exception {
        ImapFolder folder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);

        try {
            folder.getMessagesFromUids(asList("11", "22", "25"));
            fail("Expected exception");
        } catch (MessagingException e) {
            assertCheckOpenErrorMessage("Folder", e);
        }
    }

    @Test
    public void getMessagesFromUids() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);
        List<ImapResponse> imapResponses = asList(
                ImapResponseHelper.createImapResponse("* SEARCH 11"),
                ImapResponseHelper.createImapResponse("* SEARCH 22"),
                ImapResponseHelper.createImapResponse("* SEARCH 25")
        );
        when(imapConnection.executeSimpleCommand("UID SEARCH UID 11,22,25")).thenReturn(imapResponses);
        folder.open(Folder.OPEN_MODE_RW);

        List<ImapMessage> messages = folder.getMessagesFromUids(asList("11", "22", "25"));

        assertNotNull(messages);
        assertEquals(newSet("11", "22", "25"), extractMessageUids(messages));
    }

    @Test
    public void areMoreMessagesAvailable_withClosedFolder_shouldThrow() throws Exception {
        ImapFolder folder = createFolder("Folder");
        when(imapStore.getConnection()).thenReturn(imapConnection);

        try {
            folder.areMoreMessagesAvailable(10, new Date());
            fail("Expected exception");
        } catch (MessagingException e) {
            assertCheckOpenErrorMessage("Folder", e);
        }
    }

    @Test
    public void areMoreMessagesAvailable_withAdditionalMessages_shouldReturnTrue() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);
        List<ImapResponse> imapResponses = singletonList(ImapResponseHelper.createImapResponse("* SEARCH 42"));
        when(imapConnection.executeSimpleCommand("SEARCH 1:9 NOT DELETED")).thenReturn(imapResponses);
        folder.open(Folder.OPEN_MODE_RW);

        boolean result = folder.areMoreMessagesAvailable(10, null);

        assertTrue(result);
    }

    @Test
    public void areMoreMessagesAvailable_withoutAdditionalMessages_shouldReturnFalse() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);
        folder.open(Folder.OPEN_MODE_RW);

        boolean result = folder.areMoreMessagesAvailable(600, null);

        assertFalse(result);
    }

    @Test
    public void areMoreMessagesAvailable_withoutAdditionalMessages_shouldIssueSearchCommandsUntilAllMessagesSearched()
            throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);
        folder.open(Folder.OPEN_MODE_RW);

        folder.areMoreMessagesAvailable(600, null);

        verify(imapConnection).executeSimpleCommand("SEARCH 100:599 NOT DELETED");
        verify(imapConnection).executeSimpleCommand("SEARCH 1:99 NOT DELETED");
    }

    @Test
    public void fetch_withNullMessageListArgument_shouldDoNothing() throws Exception {
        ImapFolder folder = createFolder("Folder");
        FetchProfile fetchProfile = createFetchProfile();

        folder.fetch(null, fetchProfile, null);

        verifyNoMoreInteractions(imapStore);
    }

    @Test
    public void fetch_withEmptyMessageListArgument_shouldDoNothing() throws Exception {
        ImapFolder folder = createFolder("Folder");
        FetchProfile fetchProfile = createFetchProfile();

        folder.fetch(Collections.<ImapMessage>emptyList(), fetchProfile, null);

        verifyNoMoreInteractions(imapStore);
    }

    @Test
    public void fetch_withFlagsFetchProfile_shouldIssueRespectiveCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RO);
        folder.open(Folder.OPEN_MODE_RO);
        when(imapConnection.readResponse(any(ImapResponseCallback.class))).thenReturn(ImapResponseHelper.createImapResponse("x OK"));
        List<ImapMessage> messages = createImapMessages("1");
        FetchProfile fetchProfile = createFetchProfile(FetchProfile.Item.FLAGS);

        folder.fetch(messages, fetchProfile, null);

        verify(imapConnection).sendCommand("UID FETCH 1 (UID FLAGS)", false);
    }

    @Test
    public void fetch_withEnvelopeFetchProfile_shouldIssueRespectiveCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RO);
        folder.open(Folder.OPEN_MODE_RO);
        when(imapConnection.readResponse(any(ImapResponseCallback.class))).thenReturn(ImapResponseHelper.createImapResponse("x OK"));
        List<ImapMessage> messages = createImapMessages("1");
        FetchProfile fetchProfile = createFetchProfile(FetchProfile.Item.ENVELOPE);

        folder.fetch(messages, fetchProfile, null);

        verify(imapConnection).sendCommand("UID FETCH 1 (UID INTERNALDATE RFC822.SIZE BODY.PEEK[HEADER.FIELDS " +
                "(date subject from content-type to cc reply-to message-id references in-reply-to X-K9mail-Identity)]" +
                ")", false);
    }

    @Test
    public void fetch_withStructureFetchProfile_shouldIssueRespectiveCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RO);
        folder.open(Folder.OPEN_MODE_RO);
        when(imapConnection.readResponse(any(ImapResponseCallback.class))).thenReturn(ImapResponseHelper.createImapResponse("x OK"));
        List<ImapMessage> messages = createImapMessages("1");
        FetchProfile fetchProfile = createFetchProfile(FetchProfile.Item.STRUCTURE);

        folder.fetch(messages, fetchProfile, null);

        verify(imapConnection).sendCommand("UID FETCH 1 (UID BODYSTRUCTURE)", false);
    }

    @Test
    public void fetch_withBodySaneFetchProfile_shouldIssueRespectiveCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RO);
        folder.open(Folder.OPEN_MODE_RO);
        when(imapConnection.readResponse(any(ImapResponseCallback.class))).thenReturn(ImapResponseHelper.createImapResponse("x OK"));
        List<ImapMessage> messages = createImapMessages("1");
        FetchProfile fetchProfile = createFetchProfile(FetchProfile.Item.BODY_SANE);
        when(storeConfig.getMaximumAutoDownloadMessageSize()).thenReturn(4096);

        folder.fetch(messages, fetchProfile, null);

        verify(imapConnection).sendCommand("UID FETCH 1 (UID BODY.PEEK[]<0.4096>)", false);
    }

    @Test
    public void fetch_withBodySaneFetchProfileAndNoMaximumDownloadSize_shouldIssueRespectiveCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RO);
        folder.open(Folder.OPEN_MODE_RO);
        when(imapConnection.readResponse(any(ImapResponseCallback.class))).thenReturn(ImapResponseHelper.createImapResponse("x OK"));
        List<ImapMessage> messages = createImapMessages("1");
        FetchProfile fetchProfile = createFetchProfile(FetchProfile.Item.BODY_SANE);
        when(storeConfig.getMaximumAutoDownloadMessageSize()).thenReturn(0);

        folder.fetch(messages, fetchProfile, null);

        verify(imapConnection).sendCommand("UID FETCH 1 (UID BODY.PEEK[])", false);
    }

    @Test
    public void fetch_withBodyFetchProfileAndNoMaximumDownloadSize_shouldIssueRespectiveCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RO);
        folder.open(Folder.OPEN_MODE_RO);
        when(imapConnection.readResponse(any(ImapResponseCallback.class))).thenReturn(ImapResponseHelper.createImapResponse("x OK"));
        List<ImapMessage> messages = createImapMessages("1");
        FetchProfile fetchProfile = createFetchProfile(FetchProfile.Item.BODY);

        folder.fetch(messages, fetchProfile, null);

        verify(imapConnection).sendCommand("UID FETCH 1 (UID BODY.PEEK[])", false);
    }

    @Test
    public void fetch_withFlagsFetchProfile_shouldSetFlags() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RO);
        folder.open(Folder.OPEN_MODE_RO);
        List<ImapMessage> messages = createImapMessages("1");
        FetchProfile fetchProfile = createFetchProfile(FetchProfile.Item.FLAGS);
        when(imapConnection.readResponse(any(ImapResponseCallback.class)))
                .thenReturn(ImapResponseHelper.createImapResponse("* 1 FETCH (FLAGS (\\Seen) UID 1)"))
                .thenReturn(ImapResponseHelper.createImapResponse("x OK"));

        folder.fetch(messages, fetchProfile, null);

        ImapMessage imapMessage = messages.get(0);
        verify(imapMessage).setFlagInternal(Flag.SEEN, true);
    }

    @Test
    public void fetchPart_withTextSection_shouldIssueRespectiveCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RO);
        when(storeConfig.getMaximumAutoDownloadMessageSize()).thenReturn(4096);
        folder.open(Folder.OPEN_MODE_RO);
        ImapMessage message = createImapMessage("1");
        Part part = createPart("TEXT");
        when(imapConnection.readResponse(any(ImapResponseCallback.class))).thenReturn(ImapResponseHelper.createImapResponse("x OK"));

        folder.fetchPart(message, part, null);

        verify(imapConnection).sendCommand("UID FETCH 1 (UID BODY.PEEK[TEXT]<0.4096>)", false);
    }

    @Test
    public void fetchPart_withNonTextSection_shouldIssueRespectiveCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RO);
        folder.open(Folder.OPEN_MODE_RO);
        ImapMessage message = createImapMessage("1");
        Part part = createPart("1.1");
        when(imapConnection.readResponse(any(ImapResponseCallback.class))).thenReturn(ImapResponseHelper.createImapResponse("x OK"));

        folder.fetchPart(message, part, null);

        verify(imapConnection).sendCommand("UID FETCH 1 (UID BODY.PEEK[1.1])", false);
    }

    @Test
    public void appendMessages_shouldIssueRespectiveCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);
        folder.open(Folder.OPEN_MODE_RW);
        List<ImapMessage> messages = createImapMessages("1");
        when(imapConnection.readResponse()).thenReturn(ImapResponseHelper.createImapResponse("x OK [APPENDUID 1 23]"));

        folder.appendMessages(messages);

        verify(imapConnection).sendCommand("APPEND \"Folder\" () {0}", false);
    }

    @Test
    public void getUidFromMessageId_withoutMessageIdHeader_shouldReturnNull() throws Exception {
        ImapFolder folder = createFolder("Folder");
        ImapMessage message = createImapMessage("2");
        when(message.getHeader("Message-ID")).thenReturn(new String[0]);

        String result = folder.getUidFromMessageId(message);

        assertNull(result);
    }

    @Test
    public void getUidFromMessageId_withMessageIdHeader_shouldIssueUidSearchCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);
        folder.open(Folder.OPEN_MODE_RW);
        ImapMessage message = createImapMessage("2");
        when(message.getHeader("Message-ID")).thenReturn(new String[] { "<00000000.0000000@example.org>" });

        folder.getUidFromMessageId(message);

        verify(imapConnection).executeSimpleCommand("UID SEARCH HEADER MESSAGE-ID \"<00000000.0000000@example.org>\"");
    }

    @Test
    public void getUidFromMessageId() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);
        folder.open(Folder.OPEN_MODE_RW);
        ImapMessage message = createImapMessage("2");
        when(message.getHeader("Message-ID")).thenReturn(new String[] { "<00000000.0000000@example.org>" });
        when(imapConnection.executeSimpleCommand("UID SEARCH HEADER MESSAGE-ID \"<00000000.0000000@example.org>\""))
                .thenReturn(singletonList(ImapResponseHelper.createImapResponse("* SEARCH 23")));

        String result = folder.getUidFromMessageId(message);

        assertEquals("23", result);
    }

    @Test
    public void expunge_shouldIssueExpungeCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);

        folder.expunge();

        verify(imapConnection).executeSimpleCommand("EXPUNGE");
    }

    @Test
    public void setFlags_shouldIssueUidStoreCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);

        folder.setFlags(newSet(Flag.SEEN), true);

        verify(imapConnection).executeSimpleCommand("UID STORE 1:* +FLAGS.SILENT (\\Seen)");
    }

    @Test
    public void getNewPushState_withNewerUid_shouldReturnNewPushState() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);
        ImapMessage message = createImapMessage("2");

        String result = folder.getNewPushState("uidNext=2", message);

        assertEquals("uidNext=3", result);
    }

    @Test
    public void getNewPushState_withoutNewerUid_shouldReturnNull() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RW);
        ImapMessage message = createImapMessage("1");

        String result = folder.getNewPushState("uidNext=2", message);

        assertNull(result);
    }

    @Test
    public void search_withFullTextSearchEnabled_shouldIssueRespectiveCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RO);
        when(storeConfig.allowRemoteSearch()).thenReturn(true);
        when(storeConfig.isRemoteSearchFullText()).thenReturn(true);

        folder.search("query", newSet(Flag.SEEN), Collections.<Flag>emptySet());

        verify(imapConnection).executeSimpleCommand("UID SEARCH SEEN TEXT \"query\"");
    }

    @Test
    public void search_withFullTextSearchDisabled_shouldIssueRespectiveCommand() throws Exception {
        ImapFolder folder = createFolder("Folder");
        prepareImapFolderForOpen(Folder.OPEN_MODE_RO);
        when(storeConfig.allowRemoteSearch()).thenReturn(true);
        when(storeConfig.isRemoteSearchFullText()).thenReturn(false);

        folder.search("query", Collections.<Flag>emptySet(), Collections.<Flag>emptySet());

        verify(imapConnection).executeSimpleCommand("UID SEARCH OR SUBJECT \"query\" FROM \"query\"");
    }

    @Test
    public void search_withRemoteSearchDisabled_shouldThrow() throws Exception {
        ImapFolder folder = createFolder("Folder");
        when(storeConfig.allowRemoteSearch()).thenReturn(false);

        try {
            folder.search("query", Collections.<Flag>emptySet(), Collections.<Flag>emptySet());
            fail("Expected exception");
        } catch (MessagingException e) {
            assertEquals("Your settings do not allow remote searching of this account", e.getMessage());
        }
    }

    private Set<String> extractMessageUids(List<ImapMessage> messages) {
        Set<String> result = new HashSet<>();
        for (Message message : messages) {
            result.add(message.getUid());
        }

        return result;
    }

    private ImapFolder createFolder(String folderName) {
        return new ImapFolder(imapStore, folderName, FolderNameCodec.newInstance());
    }

    private ImapMessage createImapMessage(String uid) {
        ImapMessage message = mock(ImapMessage.class);
        when(message.getUid()).thenReturn(uid);

        return message;
    }

    private List<ImapMessage> createImapMessages(String... uids) {
        List<ImapMessage> imapMessages = new ArrayList<>(uids.length);

        for (String uid : uids) {
            ImapMessage imapMessage = createImapMessage(uid);
            imapMessages.add(imapMessage);
        }

        return imapMessages;
    }

    private Part createPart(String serverExtra) {
        Part part = mock(Part.class);
        when(part.getServerExtra()).thenReturn(serverExtra);

        return part;
    }

    private FetchProfile createFetchProfile(FetchProfile.Item... items) {
        FetchProfile fetchProfile = new FetchProfile();
        Collections.addAll(fetchProfile, items);

        return fetchProfile;
    }

    @SuppressWarnings("unchecked")
    private MessageRetrievalListener<ImapMessage> createMessageRetrievalListener() {
        return mock(MessageRetrievalListener.class);
    }

    private void prepareImapFolderForOpen(int openMode) throws MessagingException, IOException {
        when(imapStore.getConnection()).thenReturn(imapConnection);
        List<ImapResponse> imapResponses = asList(
                ImapResponseHelper.createImapResponse("* FLAGS (\\Answered \\Flagged \\Deleted \\Seen \\Draft NonJunk $MDNSent)"),
                ImapResponseHelper.createImapResponse("* OK [PERMANENTFLAGS (\\Answered \\Flagged \\Deleted \\Seen \\Draft NonJunk " +
                        "$MDNSent \\*)] Flags permitted."),
                ImapResponseHelper.createImapResponse("* 23 EXISTS"),
                ImapResponseHelper.createImapResponse("* 0 RECENT"),
                ImapResponseHelper.createImapResponse("* OK [UIDVALIDITY 1125022061] UIDs valid"),
                ImapResponseHelper.createImapResponse("* OK [UIDNEXT 57576] Predicted next UID"),
                (openMode == Folder.OPEN_MODE_RW) ?
                        ImapResponseHelper.createImapResponse("2 OK [READ-WRITE] Select completed.") :
                        ImapResponseHelper.createImapResponse("2 OK [READ-ONLY] Examine completed.")
        );

        if (openMode == Folder.OPEN_MODE_RW) {
            when(imapConnection.executeSimpleCommand("SELECT \"Folder\"")).thenReturn(imapResponses);
        } else {
            when(imapConnection.executeSimpleCommand("EXAMINE \"Folder\"")).thenReturn(imapResponses);
        }
    }

    private void assertCheckOpenErrorMessage(String folderName, MessagingException e) {
        assertEquals("Folder " + folderName + " is not open.", e.getMessage());
    }
}
