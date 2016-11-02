package vn.bhxh.bhxhmail.message;


import android.content.Context;
import android.content.Intent;
import android.support.annotation.VisibleForTesting;

import vn.bhxh.bhxhmail.Globals;
import vn.bhxh.bhxhmail.mail.BoundaryGenerator;
import vn.bhxh.bhxhmail.mail.MessagingException;
import vn.bhxh.bhxhmail.mail.internet.MessageIdGenerator;
import vn.bhxh.bhxhmail.mail.internet.MimeMessage;


public class SimpleMessageBuilder extends MessageBuilder {

    public static SimpleMessageBuilder newInstance() {
        Context context = Globals.getContext();
        MessageIdGenerator messageIdGenerator = MessageIdGenerator.getInstance();
        BoundaryGenerator boundaryGenerator = BoundaryGenerator.getInstance();
        return new SimpleMessageBuilder(context, messageIdGenerator, boundaryGenerator);
    }

    @VisibleForTesting
    SimpleMessageBuilder(Context context, MessageIdGenerator messageIdGenerator, BoundaryGenerator boundaryGenerator) {
        super(context, messageIdGenerator, boundaryGenerator);
    }

    @Override
    protected void buildMessageInternal() {
        try {
            MimeMessage message = build();
            queueMessageBuildSuccess(message);
        } catch (MessagingException me) {
            queueMessageBuildException(me);
        }
    }

    @Override
    protected void buildMessageOnActivityResult(int requestCode, Intent data) {
        throw new UnsupportedOperationException();
    }
}
