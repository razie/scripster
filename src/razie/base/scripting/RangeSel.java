/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.base.scripting;


/**
 * TODO 1-1 detailed docs
 * 
 * @author razvanc
 */
public class RangeSel<T> {

    public T rangeSel(int hours, int[][] range, T[] values, T dflt) {
        for (int i = 0; i < range.length; i++)
            if (hours >= range[i][0] && hours <= range[i][1])
                return values[i];
        return dflt;
    }

}
