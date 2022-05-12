package liquibase.ext.clickhouse.config.annotations;

import liquibase.ext.clickhouse.config.ClickHouseExtensionAutoconfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.DependsOn;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows to force {@link ClickHouseExtensionAutoconfiguration} instantiating.
 * Useful when liquibase initialized manually.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@DependsOn("redisHelper")
@ComponentScan(basePackageClasses = ClickHouseExtensionAutoconfiguration.class)
public @interface EnableClickhouseLiquibase {
}
