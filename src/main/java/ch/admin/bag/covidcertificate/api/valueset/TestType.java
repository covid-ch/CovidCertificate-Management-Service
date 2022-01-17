package ch.admin.bag.covidcertificate.api.valueset;

public enum TestType {
    PCR("LP6464-4", "Nucleic acid amplification with probe detection (PCR)"),
    RAPID_TEST("LP217198-3", "Rapid immunoassay"),
    ANTIBODY_TEST("94504-8", "Antibody test"),
    EXCEPTIONAL_TEST("medical-exemption", "Medical exemption");

    public final String typeCode;
    public final String typeDisplay;

    TestType(String typeCode, String typeDisplay) {
        this.typeCode = typeCode;
        this.typeDisplay = typeDisplay;
    }
}