package cobol.commons.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonBrand {

    private String name;

    @JsonProperty("stand")
    private List<CommonStand> standList = new ArrayList<>();

}
