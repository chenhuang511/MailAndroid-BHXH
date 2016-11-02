package vn.bhxh.bhxhmail.mail.store.imap;


import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;


public class ListResponseTest {
    @Test
    public void parseList_withValidResponses_shouldReturnListResponses() throws Exception {
        List<ImapResponse> responses = asList(
                ImapResponseHelper.createImapResponse("* LIST () \"/\" blurdybloop"),
                ImapResponseHelper.createImapResponse("* LIST (\\Noselect) \"/\" foo"),
                ImapResponseHelper.createImapResponse("* LIST () \"/\" foo/bar"),
                ImapResponseHelper.createImapResponse("X OK LIST completed")
        );

        List<ListResponse> result = ListResponse.parseList(responses);

        assertEquals(3, result.size());
        assertListResponseEquals(noAttributes(), "/", "blurdybloop", result.get(0));
        assertListResponseEquals(singletonList("\\Noselect"), "/", "foo", result.get(1));
        assertListResponseEquals(noAttributes(), "/", "foo/bar", result.get(2));
    }

    @Test
    public void parseList_withValidResponse_shouldReturnListResponse() throws Exception {
        List<ListResponse> result = parseSingle("* LIST () \".\" \"Folder\"");

        assertEquals(1, result.size());
        assertListResponseEquals(noAttributes(), ".", "Folder", result.get(0));
    }

    @Test
    public void parseList_withValidResponseContainingAttributes_shouldReturnListResponse() throws Exception {
        List<ListResponse> result = parseSingle("* LIST (\\HasChildren \\Noselect) \".\" \"Folder\"");

        assertEquals(1, result.size());
        assertListResponseEquals(asList("\\HasChildren", "\\Noselect"), ".", "Folder", result.get(0));
    }

    @Test
    public void parseList_withoutListResponse_shouldReturnEmptyList() throws Exception {
        List<ListResponse> result = parseSingle("* LSUB () \".\" INBOX");

        assertEquals(emptyList(), result);
    }

    @Test
    public void parseList_withMalformedListResponse1_shouldReturnEmptyList() throws Exception {
        List<ListResponse> result = parseSingle("* LIST ([inner list]) \"/\" \"Folder\"");

        assertEquals(emptyList(), result);
    }

    @Test
    public void parseList_withMalformedListResponse2_shouldReturnEmptyList() throws Exception {
        List<ListResponse> result = parseSingle("* LIST () \"ab\" \"Folder\"");

        assertEquals(emptyList(), result);
    }

    @Test
    public void parseLsub_withValidResponse_shouldReturnListResponse() throws Exception {
        List<ImapResponse> responses = singletonList(ImapResponseHelper.createImapResponse("* LSUB () \".\" \"Folder\""));

        List<ListResponse> result = ListResponse.parseLsub(responses);

        assertEquals(1, result.size());
        assertListResponseEquals(noAttributes(), ".", "Folder", result.get(0));
    }

    private List<ListResponse> parseSingle(String response) throws IOException {
        List<ImapResponse> responses = singletonList(ImapResponseHelper.createImapResponse(response));

        return ListResponse.parseList(responses);
    }

    private List<String> noAttributes() {
        return emptyList();
    }

    private void assertListResponseEquals(List<String> attributes, String delimiter, String name,
            ListResponse listResponse) {
        assertEquals(attributes, listResponse.getAttributes());
        assertEquals(delimiter, listResponse.getHierarchyDelimiter());
        assertEquals(name, listResponse.getName());
    }
}
