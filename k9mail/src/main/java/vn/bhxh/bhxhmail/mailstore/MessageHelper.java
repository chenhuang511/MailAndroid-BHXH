package vn.bhxh.bhxhmail.mailstore;


import java.util.Stack;

import vn.bhxh.bhxhmail.mail.Body;
import vn.bhxh.bhxhmail.mail.BodyPart;
import vn.bhxh.bhxhmail.mail.MessagingException;
import vn.bhxh.bhxhmail.mail.Multipart;
import vn.bhxh.bhxhmail.mail.Part;
import vn.bhxh.bhxhmail.mail.internet.MimeBodyPart;


public class MessageHelper {

    public static boolean isCompletePartAvailable(Part part) {
        Stack<Part> partsToCheck = new Stack<Part>();
        partsToCheck.push(part);

        while (!partsToCheck.isEmpty()) {
            Part currentPart = partsToCheck.pop();
            Body body = currentPart.getBody();

            boolean isBodyMissing = body == null;
            if (isBodyMissing) {
                return false;
            }

            if (body instanceof Multipart) {
                Multipart multipart = (Multipart) body;
                for (BodyPart bodyPart : multipart.getBodyParts()) {
                    partsToCheck.push(bodyPart);
                }
            }
        }

        return true;
    }

    public static MimeBodyPart createEmptyPart() {
        try {
            return new MimeBodyPart(null);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
