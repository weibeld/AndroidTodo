package org.weibeld.mytodo;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void parseDate_test() throws Exception {

        // First, test valid dimension value strings
        String[] dates = new String[] {
                "1/1/17", "Due 12/12/17", "Due 12/12/17 noon", "1 jan 2017"
        };

        Pattern pattern = Pattern.compile("(\\d\\d?)/(\\d\\d?)/(\\d\\d?)");
        for (int i = 0; i < dates.length; i++) {
            Matcher matcher = pattern.matcher(dates[i]);
            if (matcher.find()) {
                System.out.println(matcher.group() + ": group 1 = " + matcher.group(1) + ", group 2 = " + matcher.group(2) + ", group 3 = " + matcher.group(3));
            }
        }
    }
}