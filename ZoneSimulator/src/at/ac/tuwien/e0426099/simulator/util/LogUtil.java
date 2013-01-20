package at.ac.tuwien.e0426099.simulator.util;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: PatrickF
 * Date: 20.01.13
 * Time: 19:36
 * To change this template use File | Settings | File Templates.
 */
public class LogUtil {
    public static final String BR = "\n";
    public static final String TAB = "\t";
    public static final String HR1 = "=====================================================================";
    public static final String HR2 = "*********************************************************************";
    public static final String HR3 = "---------------------------------------------------------------------";

    public static String bold(String txt) {
        return txt.toUpperCase();
    }
    public static String h1(String txt) {
        return bold(txt)+BR+HR1+BR;
    }
    public static String h2(String txt) {
        return bold(txt)+BR+HR2+BR;
    }
    public static String h3(String txt) {
        return txt+BR+HR3+BR;
    }
    public static String h4(String txt) {
        return bold(txt)+BR;
    }
    public static String emptyListText(List l, String txt) {
        if(l.isEmpty()) {
            return txt+BR;
        }
        return String.valueOf("");
    }
}
