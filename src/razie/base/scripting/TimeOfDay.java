/**  ____    __    ____  ____  ____/___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___) __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__)\__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)___/   (__)  (______)(____/   LICENESE.txt
 */
package razie.base.scripting;

import java.util.Date;
import razie.base.*;

/**
 * easy to use scriptable for date-time related stuff. I use it to play with scripting and presentation etc
 * 
 * @author razvanc
 */
public class TimeOfDay {

    public static String calcvalue() {return new TimeOfDay().value();}
    
    public TimeOfDay() {
        if (aivalues == null) {
            aivalues = new ActionItem[values.length];

            for (int i = 0; i < values.length; i++)
                aivalues[i] = new ActionItem(values[i]);
        }
    }

    public String[] tags() {
        return TAGS;
    }

    public ActionItem[] values() {
        return aivalues;
    }

    public String value() {
        Date dt = new Date();
        return new RangeSel<String>().rangeSel(dt.getHours(), rangei, values, dflt);
    }

    public ActionItem aivalue() {
        Date dt = new Date();
        return new RangeSel<ActionItem>().rangeSel(dt.getHours(), rangei, aivalues, aidflt);
    }

    static String[] TAGS   = { "time" };

    int[][]         rangei = { { 0, 6 }, { 7, 11 }, { 12, 19 }, { 20, 23 } };
    public static String[]        values = { "night", "morning", "day", "evening" };
    String          dflt   = "?";

    ActionItem[]    aivalues;
    ActionItem      aidflt = new ActionItem("?");

    String[][]      ranges = { { "0", "6", "night" }, { "7", "11", "morning" }, { "12", "18", "day" },
            { "19", "23", "evening" } };
}
