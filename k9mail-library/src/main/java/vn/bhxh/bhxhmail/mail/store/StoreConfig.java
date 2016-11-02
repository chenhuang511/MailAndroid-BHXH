package vn.bhxh.bhxhmail.mail.store;


import vn.bhxh.bhxhmail.mail.NetworkType;

public interface StoreConfig {
    String getStoreUri();
    String getTransportUri();

    boolean subscribedFoldersOnly();
    boolean useCompression(NetworkType type);

    String getInboxFolderName();
    String getOutboxFolderName();
    String getDraftsFolderName();

    void setArchiveFolderName(String name);
    void setDraftsFolderName(String name);
    void setTrashFolderName(String name);
    void setSpamFolderName(String name);
    void setSentFolderName(String name);
    void setAutoExpandFolderName(String name);
    void setInboxFolderName(String name);

    int getMaximumAutoDownloadMessageSize();

    boolean allowRemoteSearch();
    boolean isRemoteSearchFullText();

    boolean isPushPollOnConnect();

    int getDisplayCount();

    int getIdleRefreshMinutes();
}
