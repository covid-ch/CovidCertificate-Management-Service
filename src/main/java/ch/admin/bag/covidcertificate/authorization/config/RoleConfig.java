package ch.admin.bag.covidcertificate.authorization.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "roles")
public class RoleConfig {

    private List<RoleData> mappings;

}
