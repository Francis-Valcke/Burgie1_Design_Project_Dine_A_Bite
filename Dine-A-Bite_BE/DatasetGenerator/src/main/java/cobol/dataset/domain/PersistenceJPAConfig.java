package cobol.dataset.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class PersistenceJPAConfig{

    //private DataSource dataSource;
    //
    //@Bean
    //public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
    //    LocalContainerEntityManagerFactoryBean em
    //            = new LocalContainerEntityManagerFactoryBean();
    //    em.setDataSource(dataSource);
    //    em.setPackagesToScan(new String[] { "cobol" });
    //
    //    JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    //    em.setJpaVendorAdapter(vendorAdapter);
    //    em.setJpaProperties(additionalProperties());
    //
    //    return em;
    //}
    //
    //@Autowired
    //public void setDataSource(DataSource dataSource) {
    //    this.dataSource = dataSource;
    //}
}
