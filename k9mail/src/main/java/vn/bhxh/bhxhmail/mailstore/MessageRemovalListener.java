package vn.bhxh.bhxhmail.mailstore;

import vn.bhxh.bhxhmail.mail.Message;

public interface MessageRemovalListener {
    public void messageRemoved(Message message);
}
