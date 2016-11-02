package vn.bhxh.bhxhmail.mail;


import android.support.annotation.VisibleForTesting;

import java.util.Locale;
import java.util.Random;


public class BoundaryGenerator {
    private static final BoundaryGenerator INSTANCE = new BoundaryGenerator(new Random());


    private final Random random;


    public static BoundaryGenerator getInstance() {
        return INSTANCE;
    }

    @VisibleForTesting
    BoundaryGenerator(Random random) {
        this.random = random;
    }

    public String generateBoundary() {
        StringBuilder sb = new StringBuilder();
        sb.append("----");
        for (int i = 0; i < 30; i++) {
            sb.append(Integer.toString(random.nextInt(36), 36));
        }
        return sb.toString().toUpperCase(Locale.US);
    }
}
