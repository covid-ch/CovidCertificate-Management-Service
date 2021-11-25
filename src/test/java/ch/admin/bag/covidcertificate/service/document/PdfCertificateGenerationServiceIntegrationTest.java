package ch.admin.bag.covidcertificate.service.document;

import ch.admin.bag.covidcertificate.api.mapper.RecoveryCertificatePdfMapper;
import ch.admin.bag.covidcertificate.api.mapper.RecoveryCertificateQrCodeMapper;
import ch.admin.bag.covidcertificate.api.mapper.TestCertificatePdfMapper;
import ch.admin.bag.covidcertificate.api.mapper.TestCertificateQrCodeMapper;
import ch.admin.bag.covidcertificate.api.mapper.VaccinationCertificatePdfMapper;
import ch.admin.bag.covidcertificate.api.mapper.VaccinationCertificateQrCodeMapper;
import ch.admin.bag.covidcertificate.api.request.Issuable;
import ch.admin.bag.covidcertificate.api.request.RecoveryCertificateCreateDto;
import ch.admin.bag.covidcertificate.api.request.TestCertificateCreateDto;
import ch.admin.bag.covidcertificate.api.request.VaccinationCertificateCreateDto;
import ch.admin.bag.covidcertificate.api.valueset.IssuableTestDto;
import ch.admin.bag.covidcertificate.api.valueset.IssuableVaccineDto;
import ch.admin.bag.covidcertificate.api.valueset.TestType;
import ch.admin.bag.covidcertificate.service.domain.AbstractCertificatePdf;
import ch.admin.bag.covidcertificate.service.domain.RecoveryCertificatePdf;
import ch.admin.bag.covidcertificate.service.domain.RecoveryCertificateQrCode;
import ch.admin.bag.covidcertificate.service.domain.TestCertificatePdf;
import ch.admin.bag.covidcertificate.service.domain.TestCertificateQrCode;
import ch.admin.bag.covidcertificate.service.domain.VaccinationCertificatePdf;
import ch.admin.bag.covidcertificate.service.domain.VaccinationCertificateQrCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import static ch.admin.bag.covidcertificate.TestModelProvider.getRecoveryCertificateCreateDto;
import static ch.admin.bag.covidcertificate.TestModelProvider.getTestCertificateCreateDto;
import static ch.admin.bag.covidcertificate.TestModelProvider.getVaccinationCertificateCreateDto;

class PdfCertificateGenerationServiceIntegrationTest {

    private final PdfCertificateGenerationService service = new PdfCertificateGenerationService();

    private final String countryEn = "Switzerland";

    private static final String familyNameBig = "WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW";
    private static final String givenNameBig = "WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW";

    private static final String familyNameMiddle = "WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW";
    private static final String givenNameMiddle = "WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW";

    private static final String familyNameSmall = "WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW";
    private static final String givenNameSmall = "WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW";

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "showWatermark", true);
    }

    private void generateDocument_vaccine(VaccinationCertificateCreateDto createDto, String language, String familyName, String givenName, String fileName) throws Exception {
        IssuableVaccineDto vaccineDto = new IssuableVaccineDto("EU/1/20/1528", "Comirnaty", "1119349007",
                "SARS-CoV-2 mRNA vaccine", "ORG-100030215",
                "Biontech Manufacturing GmbH", Issuable.CH_ONLY, false);
        ReflectionTestUtils.setField(createDto.getPersonData().getName(), "familyName", familyName);
        ReflectionTestUtils.setField(createDto.getPersonData().getName(), "givenName", givenName);
        String country = "Schweiz";

        VaccinationCertificateQrCode qrCodeData = VaccinationCertificateQrCodeMapper.toVaccinationCertificateQrCode(
                createDto, vaccineDto);
        VaccinationCertificatePdf pdfData = VaccinationCertificatePdfMapper.toVaccinationCertificatePdf(createDto,
                vaccineDto,
                qrCodeData,
                country,
                countryEn);

        doTest(pdfData, fileName, language);

    }

    private void generateDocument_vaccine(String language, String familyName, String givenName) throws Exception {
        VaccinationCertificateCreateDto createDto = getVaccinationCertificateCreateDto("1119349007", language);
        this.generateDocument_vaccine(createDto, language, familyName, givenName, "vaccine");
    }

    private void generateDocument_partialVaccination(String language, String familyName, String givenName) throws Exception {
        VaccinationCertificateCreateDto createDto = getVaccinationCertificateCreateDto("1119349007", language);
        ReflectionTestUtils.setField(createDto.getVaccinationInfo().get(0), "numberOfDoses", 1);
        this.generateDocument_vaccine(createDto, language, familyName, givenName, "partial-vaccine");
    }

    private void generateDocument_test(String language, String familyName, String givenName) throws Exception {
        TestCertificateCreateDto createDto = getTestCertificateCreateDto("test", "test", language);
        IssuableTestDto testValueSet = new IssuableTestDto("1341", "Qingdao Hightop Biotech Co., Ltd, SARS-CoV-2 Antigen Rapid Test (Immunochromatography)", TestType.RAPID_TEST);
        ReflectionTestUtils.setField(createDto.getPersonData().getName(), "familyName", familyName);
        ReflectionTestUtils.setField(createDto.getPersonData().getName(), "givenName", givenName);
        String country = "Schweiz";

        TestCertificateQrCode qrCodeData = TestCertificateQrCodeMapper.toTestCertificateQrCode(createDto, testValueSet);
        TestCertificatePdf pdfData = TestCertificatePdfMapper.toTestCertificatePdf(createDto, testValueSet, qrCodeData, country, countryEn);

        doTest(pdfData, "test", language);

    }

    private void generateDocument_recovery(String language, String familyName, String givenName) throws Exception {
        RecoveryCertificateCreateDto createDto = getRecoveryCertificateCreateDto(language);
        String country = "Schweiz";
        ReflectionTestUtils.setField(createDto.getPersonData().getName(), "familyName", familyName);
        ReflectionTestUtils.setField(createDto.getPersonData().getName(), "givenName", givenName);

        RecoveryCertificateQrCode qrCodeData = RecoveryCertificateQrCodeMapper.toRecoveryCertificateQrCode(createDto);
        RecoveryCertificatePdf pdfData = RecoveryCertificatePdfMapper.toRecoveryCertificatePdf(createDto, qrCodeData, country, countryEn);
        doTest(pdfData, "recovery", language);

    }

    @ParameterizedTest
    @MethodSource("testConfiguration")
    void generateRecoverylDocuments(String language, String familyName, String givenName) throws Exception {
        generateDocument_recovery(language, familyName, givenName);
    }

    @ParameterizedTest
    @MethodSource("testConfiguration")
    void generateVaccinationDocuments(String language, String familyName, String givenName) throws Exception {
        generateDocument_vaccine(language, familyName, givenName);
    }


    @ParameterizedTest
    @MethodSource("testConfiguration")
    void generatePartialVaccinationDocuments(String language, String familyName, String givenName) throws Exception {
        generateDocument_partialVaccination(language, familyName, givenName);
    }


    @ParameterizedTest
    @MethodSource("testConfiguration")
    void generateTestDocuments(String language, String familyName, String givenName) throws Exception {
        generateDocument_test(language, familyName, givenName);
    }

    void doTest(AbstractCertificatePdf pdfData, String filename, String language) throws Exception {

        var barcodePayload = "HC1:NCFOXNYTSFDHJI8-.O0:A%1W RI%.BI06%BF1WG21QKP85NPV*JVH5QWKIW18WA%NE/P3F/8X*G3M9FQH+4JZW4V/AY73CIBVQFSA36238FNB939PJ*KN%DJ3239L7BRNHKBWINEV40AT0C7LS4AZKZ73423ZQT-EJEG3LS4JXITAFK1HG%8SC91Z8YA7-TIP+PQE1W9L $N3-Q-*OGF2F%M RFUS2CPA-DG:A3AGJLC1788M7DD-I/2DBAJDAJCNB-439Y4.$SINOPK3.T4RZ4E%5MK9QM9DB9E%5:I9YHQ1FDIV4RB4VIOTNPS46UDBQEAJJKHHGQA8EL4QN9J9E6LF6JC1A5N11+N1X*8O13E20ZO8%3";


        byte[] document = service.generateCovidCertificate(pdfData, barcodePayload, LocalDateTime.now());

        boolean storeDocument = false;

        if (storeDocument) {
            OutputStream out = new FileOutputStream("/home/dev/Downloads/certificate-" + filename + "-" + language + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_hhmmss")) + ".pdf");
            out.write(document);
            out.close();
        }
    }

    private static Stream<Arguments> testConfiguration() {
        return Stream.of("de", "fr", "it", "rm").
                flatMap(language -> Stream.of(
                        Arguments.of(language, familyNameSmall, givenNameSmall),
                        Arguments.of(language, familyNameMiddle, givenNameMiddle),
                        Arguments.of(language, familyNameBig, givenNameBig)
                ));
    }
}