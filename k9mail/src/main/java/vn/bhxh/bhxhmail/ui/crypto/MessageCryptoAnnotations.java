package vn.bhxh.bhxhmail.ui.crypto;


import java.util.HashMap;

import vn.bhxh.bhxhmail.mail.Part;
import vn.bhxh.bhxhmail.mailstore.CryptoResultAnnotation;


public class MessageCryptoAnnotations {
    private HashMap<Part, CryptoResultAnnotation> annotations = new HashMap<>();

    MessageCryptoAnnotations() {
        // Package-private constructor
    }

    void put(Part part, CryptoResultAnnotation annotation) {
        annotations.put(part, annotation);
    }

    public CryptoResultAnnotation get(Part part) {
        return annotations.get(part);
    }

    public boolean has(Part part) {
        return annotations.containsKey(part);
    }

    public Part findKeyForAnnotationWithReplacementPart(Part part) {
        for (HashMap.Entry<Part, CryptoResultAnnotation> entry : annotations.entrySet()) {
            if (part == entry.getValue().getReplacementData()) {
                return entry.getKey();
            }
        }
        return null;
    }
}
