package ch.admin.bag.covidcertificate.api.request;

import ch.admin.bag.covidcertificate.api.exception.CreateCertificateException;
import ch.admin.bag.covidcertificate.util.DateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;

import java.time.LocalDate;

import static ch.admin.bag.covidcertificate.api.Constants.INVALID_COUNTRY_OF_TEST;
import static ch.admin.bag.covidcertificate.api.Constants.INVALID_DATE_OF_FIRST_POSITIVE_TEST_RESULT;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RecoveryCertificateDataDto {

    private static final String SWITZERLAND = "CH";

    @JsonDeserialize(using = DateDeserializer.class)
    private LocalDate dateOfFirstPositiveTestResult;

    private String countryOfTest;

    public void validate(SystemSource systemSource) {
        if (dateOfFirstPositiveTestResult == null || dateOfFirstPositiveTestResult.isAfter(LocalDate.now())) {
            throw new CreateCertificateException(INVALID_DATE_OF_FIRST_POSITIVE_TEST_RESULT);
        }
        if (countryOfTest == null) {
            throw new CreateCertificateException(INVALID_COUNTRY_OF_TEST);
        }
        final boolean isCountryCH = SWITZERLAND.equalsIgnoreCase(countryOfTest);
        switch (systemSource) {
            case WebUI: {
                break;
            }
            case CsvUpload:
            case ApiGateway: {
                // the source requires switzerland
                if (!isCountryCH) {
                    throw new CreateCertificateException(INVALID_COUNTRY_OF_TEST);
                }
                break;
            }
            case ApiPlatform: {
                // this source requires foreign countries
                if (isCountryCH) {
                    throw new CreateCertificateException(INVALID_COUNTRY_OF_TEST);
                }
                break;
            }
            default:
                throw new IllegalStateException("Attribute systemSource is invalid. Check Request implementation and/or Dto Validation.");
        }
    }
}
