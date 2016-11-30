import com.sun.org.apache.xpath.internal.operations.String;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Created by dhc on 01/12/15.
 */
public class DevTest {
    public static void main(String [] args) {

        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        System.out.println(ZonedDateTime.now().format(formatter));

        //long LongTime = System.currentTimeMillis();
        //System.out.println(getDateTimeFromTimestamp(System.currentTimeMillis()));
    }

    public LocalDateTime getDateTimeFromTimestamp(long timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        if (timestamp == 0)
            return null;
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp / 1000), TimeZone.getDefault().toZoneId());

    }

    public  LocalDate getDateFromTimestamp(long timestamp) {
        LocalDateTime date = getDateTimeFromTimestamp(timestamp);
        return date == null ? null : date.toLocalDate();
    }
}

