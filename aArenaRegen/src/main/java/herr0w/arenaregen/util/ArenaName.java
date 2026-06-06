package herr0w.arenaregen.util;

import java.util.Locale;
import java.util.regex.Pattern;

public final class ArenaName {
    private static final Pattern VALID = Pattern.compile("[A-Za-z0-9_-]{1,64}");

    private ArenaName() {
    }

    public static boolean valid(String name) {
        return VALID.matcher(name).matches();
    }

    public static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
