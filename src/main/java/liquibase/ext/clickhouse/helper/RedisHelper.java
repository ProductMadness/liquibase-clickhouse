package liquibase.ext.clickhouse.helper;

import lombok.Getter;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Helper class to access {@link RedisTemplate} from non-spring managed class.
 */
public class RedisHelper {

    @Getter
    private static RedisTemplate<String, String> redisLockTemplate;

    public RedisHelper(RedisTemplate<String, String> redisTemplate) {
        redisLockTemplate = redisTemplate;
    }
}
