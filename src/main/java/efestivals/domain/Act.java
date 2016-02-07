package efestivals.domain;

import domain.Show;

/**
 * Created by Adam on 23/04/2015.
 */
public class Act implements Show {
    private String name;
    private String day;
    private String stage;
    private String status;
    private String scrobs;
    private Integer recrank;

    public Act() {}

    public Act(String name, String day, String stage, String status) {
        this.name = name;
        this.day = day;
        this.stage = stage;
        this.status = status;
    }

    public Act(Act act, String scrobs, Integer recrank) {
        this(act.getName(), act.getDay(), act.getStage(), act.getStatus());
        this.scrobs = scrobs;
        this.recrank = recrank;
    }

    public String getName() {
        return name;
    }

    public String getDay() {
        return day;
    }

    public String getStage() {
        return stage;
    }

    public String getStatus() {
        return status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getScrobs() {
        return scrobs;
    }

    public void setScrobs(String scrobs) {
        this.scrobs = scrobs;
    }

    public Integer getRecrank() {
        return recrank;
    }

    public void setRecrank(Integer recrank) {
        this.recrank = recrank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Act act = (Act) o;

        if (day != null ? !day.equals(act.day) : act.day != null) return false;
        if (name != null ? !name.equals(act.name) : act.name != null) return false;
        if (stage != null ? !stage.equals(act.stage) : act.stage != null) return false;
        if (status != null ? !status.equals(act.status) : act.status != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (day != null ? day.hashCode() : 0);
        result = 31 * result + (stage != null ? stage.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "pojo.Act{" +
                "name='" + name + '\'' +
                ", day='" + day + '\'' +
                ", stage='" + stage + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
