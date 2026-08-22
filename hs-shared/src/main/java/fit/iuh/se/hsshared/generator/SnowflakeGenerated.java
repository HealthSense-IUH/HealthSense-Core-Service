package fit.iuh.se.hsshared.generator;

import fit.iuh.se.hsshared.generator.impl.SnowflakeIdGenerator;
import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@IdGeneratorType(SnowflakeIdGenerator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface SnowflakeGenerated {
}
