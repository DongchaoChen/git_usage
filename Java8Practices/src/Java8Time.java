import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by dhc on 07/12/15.
 */
public class Java8Time {

    public static void main(String[] args) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        System.out.println("Test case 1 - Use ISO_INSTANT directly from ZonedDateTime.now() " + ZonedDateTime.now().format(formatter));

        long timeFromCassandra = 1449480692304L;
        System.out.println("Test case 2 - Convert from milliSinceEpoch to ZonedDateTime " + toJavaZonedDateTimeFromEpochMilli(timeFromCassandra));
        System.out.println("Test case 3 - Convert from milliSinceEpoch to ZonedDateTime with ISO_INSTANT " +
                toJavaZonedDateTimeFromEpochMilli(timeFromCassandra).format(DateTimeFormatter.ISO_INSTANT));

        String dateTimeCase1 = "2015-12-07T09:31:32.304Z";
        String dateTimeCase2 = "2011-02-03T04:05+0000";
        String dateTimeCase3 = "2011-02-03T04:05:00+0000";

        System.out.println("Test case 4 - Convert from string 2011-02-03T04:05:00.000Z to ZonedDateTime " + toJavaZonedDateTimeFromString(dateTimeCase1));
        //System.out.println("Test case 5 - Convert from string 2011-02-03T04:05+0000 to ZonedDateTime " + toJavaZonedDateTimeFromString(dateTimeCase2));
        //System.out.println("Test case 6 - Convert from string 2011-02-03T04:05:00+0000 to ZonedDateTime " + toJavaZonedDateTimeFromString(dateTimeCase3));

    }


    //Help methods
    static public java.util.Date toJavaUtilDateFromZoneDateTime( ZonedDateTime zdt) {
        Instant instant = zdt.toInstant();
        long millisecondsSinceEpoch = instant.toEpochMilli();
        return new java.util.Date(millisecondsSinceEpoch);
    }

    static public java.time.ZonedDateTime toJavaZonedDateTimeFromEpochMilli( long milliSinceEpoch )
    {
        Instant instant = Instant.ofEpochMilli(milliSinceEpoch);
        return ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
    }

    static public java.time.ZonedDateTime toJavaZonedDateTimeFromString( String timestampStr )
    {
        return ZonedDateTime.parse(timestampStr, DateTimeFormatter.ISO_DATE_TIME);
    }



}
