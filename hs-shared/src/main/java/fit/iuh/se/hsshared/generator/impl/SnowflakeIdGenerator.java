package fit.iuh.se.hsshared.generator.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.extra.spring.SpringUtil;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.springframework.stereotype.Component;

@Component
public class SnowflakeIdGenerator implements IdentifierGenerator {

    @Override
    public Object generate(
            SharedSessionContractImplementor sharedSessionContractImplementor, Object o) {
        Snowflake snowflake = SpringUtil.getBean(Snowflake.class);
        return snowflake.nextId();
    }
}
