package dk.kb.util;

import org.junit.jupiter.api.Test;

import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

public class DatetimeParserTest {
    private static final String dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss[XX][XXX]";

    @Test
    void testRepairZonedDateTimeOffset() throws MalformedIOException {
        String date1 = "2008-02-12T06:30:00+0100"; // no ":" test
        assertEquals(DatetimeParser.parseStringToZonedDateTime(date1,dateTimeFormat).toString(),
                "2008-02-12T06:30+01:00");

        String date2 = "2008-02-12T06:30:00+01:00"; // with ":" test
        assertEquals(DatetimeParser.parseStringToZonedDateTime(date2,dateTimeFormat).toString(),
                "2008-02-12T06:30+01:00");

        String date3 = "[2008-02-12T06:30:00+0100]"; // Bracket test
        assertEquals(DatetimeParser.parseStringToZonedDateTime(date3,dateTimeFormat).toString(),
                "2008-02-12T06:30+01:00");

        String date4 = " 2008-02-12T06:30 :00 +0100 "; // Space test
        assertEquals(DatetimeParser.parseStringToZonedDateTime(date4,dateTimeFormat).toString(),
                "2008-02-12T06:30+01:00");

        String date5 = "2008-02-12T06:30:00+01000"; // 1 zero extra in timezone
        assertEquals(DatetimeParser.parseStringToZonedDateTime(date5,dateTimeFormat).toString(),
                "2008-02-12T06:30+01:00");

        String date6 = " [2008 -02 -12 T06:30:00+01000] "; // Multi test
        assertEquals(DatetimeParser.parseStringToZonedDateTime(date6,dateTimeFormat).toString(),
                "2008-02-12T06:30+01:00");

        String date7 = "2008-02-12T06:30:00+010"; // Wrong number og zeroes in timezone
        assertEquals(DatetimeParser.parseStringToZonedDateTime(date7,dateTimeFormat).toString(),
                "2008-02-12T06:30+01:00");

        String date8 = "2008-02-12T06:30:00+00000100000000"; // Wrong number og zeroes in timezone Version 2
        assertEquals(DatetimeParser.parseStringToZonedDateTime(date8,dateTimeFormat).toString(),
                "2008-02-12T06:30+01:00");

        String date9 = "2008-02-12T06:30:00+000000012000000"; // Wrong number og zeroes in timezone Version 3
        assertEquals(DatetimeParser.parseStringToZonedDateTime(date9,dateTimeFormat).toString(),
                "2008-02-12T06:30+12:00");

        String date10 = "åååå-mm-ddTtt:mm:ss+0200"; // Garbage data
        assertThrowsExactly(MalformedIOException.class, ()-> DatetimeParser.parseStringToZonedDateTime(date10,dateTimeFormat));
    }

    @Test
    void zuluTimestampTest() throws MalformedIOException {
        String date1 = "1967-12-19T16:40Z";
        assertEquals("1967-12-19T16:40Z", DatetimeParser.parseStringToZonedDateTime(date1, dateTimeFormat).toString());
        assertEquals("1967-12-19T16:40:00Z", DatetimeParser.parseStringToZonedDateTime(date1, dateTimeFormat).format(DateTimeFormatter.ISO_INSTANT));
    }

    @Test
    void noTInPlusTwoTimezone() throws MalformedIOException {
        String date = "1987-05-0416:45:00+0200";
        assertEquals("1987-05-04T14:45:00Z", DatetimeParser.parseStringToZonedDateTime(date, dateTimeFormat).format(DateTimeFormatter.ISO_INSTANT));
    }
}

