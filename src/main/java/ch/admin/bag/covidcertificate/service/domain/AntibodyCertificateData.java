package ch.admin.bag.covidcertificate.service.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AntibodyCertificateData {
    @JsonProperty("tg")
    private String diseaseOrAgentTargeted;
    @JsonProperty("tt")
    private String typeOfTest;
    @JsonProperty("ma")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String testManufacturer;
    @JsonProperty("sc")
    private ZonedDateTime sampleDateTime;
    @JsonProperty("tr")
    private String result;
    @JsonProperty("tc")
    private String testingCentreOrFacility;
    @JsonProperty("co")
    private String memberStateOfTest;
    @JsonProperty("is")
    private String issuer;
    @JsonProperty("ci")
    private String identifier;
}
