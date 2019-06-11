package com.pakybytes.sys.services.os;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.prefs.Preferences;


/**
 * @author Hugo Tigre
 * @since 1.0.0
 */
public class OSUtils {

    private static String os = null;

    private static final String WIN = "win";
    private static final String MAC = "mac";
    private static final String UNI = "uni";
    private static final String SOL = "sol";


    /**
     * @return The operating system version unfiltered.
     */
    public String getOSRaw() {
        return getOSFromJVM();
    }


    /**
     * Tries to determine the Operating System type. It only tries the first time it
     * is called, subsequent requests return saved value.
     *
     * @return "win" for Windows, "mac" for MacOS, "uni" for Unix/Linux or "sol" for Solaris
     */
    public String getOS() {
        if (isWindows()) return WIN;
        if (isMac()) return MAC;
        if (isUnix()) return UNI;
        if (isSolaris()) return SOL;
        return "n/a";
    }


    /**
     * This function only exists to provide lazy initialization
     */
    private static String getOSFromJVM() {
        if (OSUtils.os == null) {
            OSUtils.os = System.getProperty("os.name").toLowerCase();
        }
        return OSUtils.os;
    }


    public boolean isWindows() {
        return getOSFromJVM().contains(WIN);
    }


    public boolean isMac() {
        return getOSFromJVM().contains(MAC);
    }


    public boolean isUnix() {
        return getOSFromJVM().contains("nix") ||
                getOSFromJVM().contains("nux") ||
                getOSFromJVM().contains("aix");
    }


    public boolean isSolaris() {
        return getOSFromJVM().contains("sunos");
    }


    /**
     * Tries to determine if user has administrator privileges each time it is called.
     * You should cache this value if you are planning to call it multiple times and don't
     * want to account for updated privileges.
     *
     * @return 0 = False, 1 = True, -1 = Error
     * @throws SysOSException if system is not supported
     */
    public int userIsAdmin() {
        try {
            if (isMac()) return userIsAdminMac();
            return userIsAdminOther();
        } catch (Exception ex) {
            throw new SysOSException(ex.getCause());
        }
    }


    private int userIsAdminOther() {
        Preferences prefs = Preferences.systemRoot();

        try {
            prefs.put("foo", "bar"); // SecurityException on Windows
            prefs.remove("foo");
            prefs.flush(); // BackingStoreException on Linux
            return 1;
        } catch (Exception ex) {
            return 0;
        }
    }


    private int userIsAdminMac() throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("id -Gn");

        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        process.waitFor();
        int exitValue = process.exitValue();
        String exitLine = br.readLine();

        if (exitValue != 0 || exitLine == null || exitLine.isEmpty())
            return -1;

        if (exitLine.matches(".*\\badmin\\b.*"))
            return 1;

        return 0;
    }


    private static class SysOSException extends RuntimeException {
        SysOSException(Throwable ex) {
            super(ex);
        }
    }
}
