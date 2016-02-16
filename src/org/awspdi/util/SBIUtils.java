package org.awspdi.util;

import java.io.*;

/**
 * Created by roberto on 2/16/16.
 */
public class SBIUtils {

    /***
     * Runs a shell command, returning the STDOUT output as a string. Does not capture STDERR.
     * @param cmd The command to run, i.e. "ls"
     * @return A string containing the command's output.
     */
    public static String runCmdWithOutString(String cmd) {

        StringWriter sw = new StringWriter();

        try (InputStream in = Runtime.getRuntime().exec(cmd).getInputStream();
             BufferedReader rdr = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = rdr.readLine()) != null) {
                sw.write(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return sw.toString();

    }

}
