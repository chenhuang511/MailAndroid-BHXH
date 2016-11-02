package vn.bhxh.bhxhmail.mail.internet;


import org.apache.james.mime4j.util.MimeUtil;
import org.junit.Test;

import java.io.IOException;

import okio.Buffer;
import vn.bhxh.bhxhmail.mail.MessagingException;

import static org.junit.Assert.assertEquals;


public class TextBodyTest {
    @Test
    public void getSize_withSignUnsafeData_shouldReturnCorrectValue() throws Exception {
        TextBody textBody = new TextBody("From Bernd");
        textBody.setEncoding(MimeUtil.ENC_QUOTED_PRINTABLE);

        long result = textBody.getSize();
        
        int outputSize = getSizeOfSerializedBody(textBody);
        assertEquals(outputSize, result);
    }

    private int getSizeOfSerializedBody(TextBody textBody) throws IOException, MessagingException {
        Buffer buffer = new Buffer();
        textBody.writeTo(buffer.outputStream());
        return buffer.readByteString().size();
    }
}
