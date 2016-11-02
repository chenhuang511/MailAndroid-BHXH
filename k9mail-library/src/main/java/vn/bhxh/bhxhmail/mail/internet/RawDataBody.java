package vn.bhxh.bhxhmail.mail.internet;


import vn.bhxh.bhxhmail.mail.Body;


/**
 * See {@link MimeUtility#decodeBody(Body)}
 */
public interface RawDataBody extends Body {
    String getEncoding();
}
