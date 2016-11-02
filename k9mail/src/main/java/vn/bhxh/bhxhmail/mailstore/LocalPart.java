package vn.bhxh.bhxhmail.mailstore;


public interface LocalPart {
    String getAccountUuid();
    long getId();
    long getSize();
    LocalMessage getMessage();
}
