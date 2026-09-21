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
        assertEquals("2008-02-12T06:30+01:00",
                DatetimeParser.parseStringToZonedDateTime(date1, dateTimeFormat).toString());

        String date2 = "2008-02-12T06:30:00+01:00"; // with ":" test
        assertEquals("2008-02-12T06:30+01:00",
                DatetimeParser.parseStringToZonedDateTime(date2, dateTimeFormat).toString());

        String date3 = "[2008-02-12T06:30:00+0100]"; // Bracket test
        assertEquals("2008-02-12T06:30+01:00",
                DatetimeParser.parseStringToZonedDateTime(date3, dateTimeFormat).toString());

        String date4 = " 2008-02-12T06:30 :00 +0100 "; // Space test
        assertEquals("2008-02-12T06:30+01:00",
                DatetimeParser.parseStringToZonedDateTime(date4, dateTimeFormat).toString());

        String date5 = "2008-02-12T06:30:00+01000"; // 1 zero extra in timezone
        assertEquals("2008-02-12T06:30+01:00",
                DatetimeParser.parseStringToZonedDateTime(date5, dateTimeFormat).toString());

        String date6 = " [2008 -02 -12 T06:30:00+01000] "; // Multi test
        assertEquals("2008-02-12T06:30+01:00",
                DatetimeParser.parseStringToZonedDateTime(date6, dateTimeFormat).toString());

        String date6WithNegativeOffset = " [2008 -02 -12 T06:30:00-01000] "; // Multi test
        assertEquals("2008-02-12T06:30-01:00",
                DatetimeParser.parseStringToZonedDateTime(date6WithNegativeOffset, dateTimeFormat).toString());

        String date7 = "2008-02-12T06:30:00+010"; // Wrong number of zeroes in UTC offset
        assertEquals("2008-02-12T06:30+01:00",
                DatetimeParser.parseStringToZonedDateTime(date7, dateTimeFormat).toString());

        String date8 = "2008-02-12T06:30:00+00000100000000"; // Wrong number of zeroes in UTC offset Version 2
        assertEquals("2008-02-12T06:30+01:00",
                DatetimeParser.parseStringToZonedDateTime(date8, dateTimeFormat).toString());

        String date8WithMinus = "2008-02-12T06:30:00+00000100000000"; // Wrong number of zeroes in UTC offset Version 2
        assertEquals("2008-02-12T06:30+01:00",
                DatetimeParser.parseStringToZonedDateTime(date8WithMinus, dateTimeFormat).toString());

        String date9 = "2008-02-12T06:30:00+000000012000000"; // Wrong number of zeroes in UTC offset Version 3
        assertEquals("2008-02-12T06:30+12:00",
                DatetimeParser.parseStringToZonedDateTime(date9, dateTimeFormat).toString());

        String date10 = "åååå-mm-ddTtt:mm:ss+0200"; // Garbage data
        assertThrowsExactly(MalformedIOException.class,
                () -> DatetimeParser.parseStringToZonedDateTime(date10, dateTimeFormat));
    }

    @Test
    void zuluTimestampTest() throws MalformedIOException {
        String date1 = "1967-12-19T16:40Z";
        assertEquals("1967-12-19T16:40Z",
                DatetimeParser.parseStringToZonedDateTime(date1, dateTimeFormat).toString());
        assertEquals("1967-12-19T16:40:00Z",
                DatetimeParser.parseStringToZonedDateTime(date1, dateTimeFormat).format(DateTimeFormatter.ISO_INSTANT));
    }

    @Test
    void noTInPlusTwoTimezone() throws MalformedIOException {
        String date = "1987-05-0416:45:00+0200";
        assertEquals("1987-05-04T14:45:00Z",
                DatetimeParser.parseStringToZonedDateTime(date, dateTimeFormat).format(DateTimeFormatter.ISO_INSTANT));
    }

    @Test
    void getsTimeOfDay() {
        assertEquals("15:53", DatetimeParser.getTimePart("2026-09-16T15:53+02:00"));
        assertEquals("11:53:00", DatetimeParser.getTimePart("2026-09-16T11:53:00-02:00"));
        assertEquals("13:53:00", DatetimeParser.getTimePart("2026-09-16T13:53:00Z"));
        // Lower case z
        assertEquals("13:53:00", DatetimeParser.getTimePart("2026-09-16T13:53:00z"));
    }

}

