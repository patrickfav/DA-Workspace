package at.ac.tuwien.e0426099.simulator.helper.util;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: PatrickF
 * Date: 26.01.13
 * Time: 13:27
 * To change this template use File | Settings | File Templates.
 */
public class DateUtil {

    /**
     * Returns the elapsed ms betwen those 2 dates
     * Will return -1 if date 1 is older than date 2.
     *
     * @param begin
     * @param end end
     * @return
     */
    public static long elapsedTime(Date begin,Date end) {
        if(begin.getTime() > end.getTime())
            return -1;

        return end.getTime()-begin.getTime();
    }
}
