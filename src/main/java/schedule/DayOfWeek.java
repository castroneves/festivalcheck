package schedule;

/**
 * Created by Adam on 11/02/2016.
 */
public enum DayOfWeek {
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY,
    MONDAY,
    TUESDAY;

    public int getValue() {
        return ordinal() + 1;
    }
}
