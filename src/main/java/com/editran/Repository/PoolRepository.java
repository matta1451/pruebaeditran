package com.editran.Repository;

import com.editran.variable.EncriptadorGeneral;
import com.editran.variable.Util;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class PoolRepository {

    private final Logger log = LogManager.getLogger(PoolRepository.class);

    @Bean(name = "datasource")
    @ConfigurationProperties(prefix = "spring.datasource")
    @Primary
    public DataSource conexionCesullBD() {
        DataSourceBuilder dsb = DataSourceBuilder.create();
        dsb.url(Util.getPro().getProperty("spring.datasource.url"));
        dsb.username(Util.getPro().getProperty("spring.datasource.username"));
        String password = EncriptadorGeneral.decrypt(Util.getPro().getProperty("spring.datasource.password"), Util.getPro().getProperty("security.keyencryptor"));
        dsb.password(password);
        dsb.driverClassName(Util.getPro().getProperty("spring.datasource.driver-class-name"));
        return dsb.build();
    }

    @Bean(name = "conexionCesull")
    @Autowired
    public JdbcTemplate jdbcCesull(@Qualifier("datasource") DataSource cesull) {
        return new JdbcTemplate(cesull);
    }
}
