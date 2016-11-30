import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.TimeZone;

/**
 * Created by dhc on 01/12/15.
 */
public class Test2 {

    static final DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

    public static void main(String[] args) {
        //DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        //System.out.println(ZonedDateTime.now());
        //System.out.println(ZonedDateTime.now().format(formatter));

        try
        {
//            long longTime = System.currentTimeMillis();
//            System.out.println(getDateTimeFromTimestamp(longTime));
//
//            String temp = getDateTimeFromTimestamp(longTime).format(formatter);
//            System.out.println(getDateTimeFromString(temp));


            String date = "2015-12-01T18:23:13.994Z";
            System.out.println(getDateTimeFromString(date).format(formatter));
        }
        catch( DateTimeParseException e) {
            throw new DateTimeParseException(e.getMessage(), e.getParsedString(), e.getErrorIndex());
        }

        System.out.println(System.currentTimeMillis());
        //System.out.println(getDateTimeFromTimestamp(System.currentTimeMillis()));
        //System.out.println(getDateTimeFromTimestamp(1400262790567L));

    }

//    public static String getDateTimeFromTimestamp(long timestamp) {
//        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
//        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
//        //.withZone(ZoneOffset.UTC)
//
//        //"yyyy-MM-dd'T'HH:mm:ssXXX"
//        //"yyyy-mm-dd'T'HH:mm:ssZ"
//        if (timestamp == 0)
//            return null;
//        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), TimeZone.getDefault().toZoneId()).format(formatter2);
//    }
//
//    public static String getDateTimeFromString(String str)
//    {
//        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
//        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
//
//        if(str != null && str.length()>0 )
//        {
//            return ZonedDateTime.parse(str).format(formatter);
//        }
//        return null;
//    }

    public static ZonedDateTime getDateTimeFromTimestamp(long timestamp) {
        if (timestamp == 0)
            return null;
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC"));

    }

    public static ZonedDateTime getDateTimeFromString(String str)
    {
        if(str != null && str.length()>0 )
        {
            return ZonedDateTime.parse(str);
        }
        return null;
    }


}
