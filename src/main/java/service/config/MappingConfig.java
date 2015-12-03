package service.config;

import java.util.List;

/**
 * Created by Adam on 01/12/2015.
 */
public class MappingConfig {

    private List<MappingTuple> mappings;

    public List<MappingTuple> getMappings() {
        return mappings;
    }

    public void setMappings(List<MappingTuple> mappings) {
        this.mappings = mappings;
    }

    public MappingConfig(List<MappingTuple> mappings) {
        this.mappings = mappings;
    }
}
