package glasto;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;
import pojo.Act;

import static java.util.stream.Collectors.toList;

/**
 * Created by Adam on 23/04/2015.
 */
public class GlastoResponseParser {

    private Map<Character,String> dayNames = new HashMap<>();
    private Map<String,String> statusMap = new HashMap<>();

    public GlastoResponseParser() {
        dayNames.put('1', "Wednesday");
        dayNames.put('2', "Thursday");
        dayNames.put('3', "Friday");
        dayNames.put('4', "Saturday");
        dayNames.put('5', "Sunday");
        dayNames.put('6', "Day Unknown");

        statusMap.put("TBC", "To Be Confirmed");
        statusMap.put("C", "Confirmed");
        statusMap.put("R", "Rumour");
        statusMap.put("SR", "Strong Rumour");
    }

    public List<Act> parseRawResponse(String rawResponse) {
        Splitter splitter = Splitter.on("<div id=\"panel").omitEmptyStrings();
        List<String> days = splitter.splitToList(rawResponse);
        return days.subList(1,days.size()).stream().flatMap(x -> parseDay(x).stream()).collect(toList());
    }

    private List<Act> parseDay(String day) {
        char panelNo = day.charAt(0);
        String dayName = dayNames.get(panelNo);
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
        String blah = stageName;
        return bands.subList(1,bands.size()).stream().map(x -> parseBand(x, blah, dayName)).collect(toList());
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
