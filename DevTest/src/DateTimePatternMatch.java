import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dhc on 06/01/16.
 */
public class DateTimePatternMatch {

    //Cassandra ISO 8601 format
    //yyyy-mm-dd HH:mm
    //yyyy-mm-dd HH:mm:ss
    //yyyy-mm-dd HH:mmZ
    //yyyy-mm-dd HH:mm:ssZ
    //yyyy-mm-dd'T'HH:mm
    //yyyy-mm-dd'T'HH:mmZ
    //yyyy-mm-dd'T'HH:mm:ss
    //yyyy-mm-dd'T'HH:mm:ssZ
    //yyyy-mm-dd
    //yyyy-mm-ddZ

    public static void main(String[] args) {

        List dates = new ArrayList();

        dates.add("2011-07-13");
        dates.add("2011/07/13");
        dates.add("2015-02-03T07:05:00.240Z");
        dates.add("2014/05/03T10:20:00");
        //dates.add("***wid");

        for(int i = 0; i<dates.size(); i++)
        {
            System.out.println(dates.get(i).toString() + " " + isDate(dates.get(i).toString()));
            System.out.println(convertToLuceneDate(dates.get(i).toString()));
        }

    }

    private static boolean isDate(String fieldValue) {
        //Date string contains yyyy-mm-dd or yyyy/mm/dd minimum length is 10
        String regex = "^[0-9]{4}[-/](1[0-2]|0[1-9])[-/](3[01]|[12][0-9]|0[1-9])$";
        Pattern pattern = Pattern.compile(regex);

        return (fieldValue == null || fieldValue.length() < 10) ? false : pattern.matcher(fieldValue.substring(0, 10)).matches();
    }

    public static String convertToLuceneDate(String fieldValue) {
        if (isDate(fieldValue)) {
            try {
                String dateValue = fieldValue.replaceAll("/", "-");
                String dateUTCValue = dateValue.length() == 10 ?
                        LocalDate.parse(dateValue).atStartOfDay(ZoneOffset.UTC).format(java.time.format.DateTimeFormatter.ISO_DATE_TIME).toString() :
                        ZonedDateTime.parse(dateValue, java.time.format.DateTimeFormatter.ISO_DATE_TIME).toString();
                dateUTCValue = dateUTCValue.replaceAll("-", "/").replace("T", " ");
                return dateUTCValue.length() == "yyyy-MM-ddThh:mm:ss.SSSZ".length() ?
                        dateUTCValue.replace("Z", " Z") :
                        dateUTCValue.replace("Z", ".000 Z");
            } catch (DateTimeParseException e) {
                throw new DateTimeParseException("Support ISO-8601 UTC timestamp string yyyy-mm-dd'T'HH:mm:ssZ (e.g. 2011-02-03T04:05:00.000Z), DateTimeParseException ",
                        e.getParsedString(), e.getErrorIndex());
            } catch (DateTimeException e) {
                throw new DateTimeException(
                        "Support ISO-8601 UTC timestamp string yyyy-mm-dd'T'HH:mm:ssZ (e.g. 2011-02-03T04:05:00.000Z), DateTimeException "
                                + e.getMessage());
            }
        }
        throw new IllegalArgumentException(String.format("No Lucene Date String conversion available for value '%s'.", fieldValue));
    }

    public static String convertDateFormat(String fieldValue) {
       return null;
    }

    static java.util.Date toJavaUtilDateFromString(String timestampStr) {
        Instant instant = ZonedDateTime.parse(timestampStr, java.time.format.DateTimeFormatter.ISO_DATE_TIME).toInstant();
        return new java.util.Date(instant.toEpochMilli());
    }

}
