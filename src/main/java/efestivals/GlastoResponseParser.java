package efestivals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import com.google.common.base.Splitter;
import efestivals.domain.Act;

import static java.util.stream.Collectors.toConcurrentMap;
import static java.util.stream.Collectors.toList;

/**
 * Created by Adam on 23/04/2015.
 */
public class GlastoResponseParser {

    private Map<String,String> abvDays = new HashMap<>();

    private Map<String,String> statusMap = new HashMap<>();

    public GlastoResponseParser() {
        abvDays.put("Mon", "Monday");
        abvDays.put("Tue", "Tuesday");
        abvDays.put("Wed", "Wednesday");
        abvDays.put("Thu", "Thursday");
        abvDays.put("Fri", "Friday");
        abvDays.put("Sat", "Saturday");
        abvDays.put("Sun", "Sunday");

        statusMap.put("TBC", "To Be Confirmed");
        statusMap.put("C", "Confirmed");
        statusMap.put("R", "Rumour");
        statusMap.put("SR", "Strong Rumour");
    }

    public List<Act> parseRawResponse(String rawResponse) {
        Splitter splitter = Splitter.on("<div id=\"panel").omitEmptyStrings();
        Map<Integer,String> dayNamez = fetchDayNames(rawResponse);

        List<String> days = splitter.splitToList(rawResponse);
        return days.subList(1,days.size()).stream().flatMap(x -> parseDay(x, dayNamez).stream()).collect(toList());
    }

    private Map<Integer, String> fetchDayNames(String rawResponse) {
        Splitter splitter = Splitter.on("<li><a href=\"#panel").omitEmptyStrings();
        List<String> strings = splitter.splitToList(rawResponse);
        List<String> collect = strings.stream().map(this::fetchDayName).filter(x -> !x.isEmpty()).collect(toList());
        List<String> dayNames = collect.stream().map(x -> abvDays.get(x) == null ? x : abvDays.get(x)).collect(toList());
        return IntStream.range(1,dayNames.size() + 1)
                .mapToObj(x -> new Integer(x))
                .collect(toConcurrentMap(x -> x, y -> dayNames.get(y - 1)));
    }

    private String fetchDayName(String row) {
        Pattern pattern = Pattern.compile("\">(.*)</a></li>");
        Matcher matcher = pattern.matcher(row);
        String day = "";
        if(matcher.find()) {
            day = matcher.group(1).split(" ")[0];
        }
        return day;
    }

    private List<Act> parseDay(String day, Map<Integer,String> dayNames) {
        char panelNo = day.charAt(0);
        String dayName = dayNames.get(Character.getNumericValue(panelNo));
        if(day == null) {
            return Arrays.asList();
        }
        Splitter splitter = Splitter.on("<li class=\"stage\"><div class=\"stagename\">").omitEmptyStrings();
        List<String> stages = splitter.splitToList(day);
        return stages.subList(1,stages.size()).stream().flatMap(x -> parseStage(x, dayName).stream()).collect(toList());
    }

    private List<Act> parseStage(String stage, String dayName) {
        Pattern pattern = Pattern.compile("^(.*)</div>");
        Matcher matcher = pattern.matcher(stage);
        String stageName="";
        if(matcher.find()) {
            stageName = matcher.group(1);
        }
        Splitter splitter = Splitter.on("<div class=\"band\">").omitEmptyStrings();
        List<String> bands = splitter.splitToList(stage);
        String stageNameCopy = stageName;
        return bands.subList(1,bands.size()).stream().map(x -> parseBand(x, stageNameCopy, dayName)).collect(toList());
    }

    private Act parseBand(String band, String stageName, String dayName) {
        Pattern statusPattern = Pattern.compile("<span class=\".*\">[(](.*)[)]</span>(?!</a>)");
        Matcher statusMatcher = statusPattern.matcher(band);
        String status = "";
        if (statusMatcher.find()) {
            status = statusMatcher.group(1);
        }
        Pattern bandPattern = Pattern.compile(">([^<>]+)</span></a>");
        Matcher bm1 = bandPattern.matcher(band);
        String bandName;
        if(bm1.find()) {
            bandName = bm1.group(1);
        } else {
            Pattern bandPattern2 = Pattern.compile("</span>(.*)</a>");
            Matcher bm2 = bandPattern2.matcher(band);
            if(bm2.find()) {
                bandName = bm2.group(1);
            } else {
                throw new RuntimeException("Parser unable to determine bandname");
            }
        }
        return new Act(bandName.trim(),dayName,stageName,statusMap.get(status));
    }
}
