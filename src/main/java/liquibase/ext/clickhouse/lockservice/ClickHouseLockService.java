/*-
 * #%L
 * Liquibase extension for Clickhouse
 * %%
 * Copyright (C) 2020 - 2022 Mediarithmics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package liquibase.ext.clickhouse.lockservice;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.exception.LockException;
import liquibase.ext.clickhouse.database.ClickHouseDatabase;
import liquibase.ext.clickhouse.helper.RedisHelper;
import liquibase.lockservice.DatabaseChangeLogLock;
import liquibase.lockservice.LockService;
import liquibase.logging.Logger;
import lombok.val;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Lock service for the ClickHouse DB.
 * Since ClickHouse itself can't return number of rows modified with {@code ALTER TABLE .. UPDATE ..} statement
 * and doesn't support locking mechanism - it makes impossible to implement lock within this DB.
 * <p>
 * Assuming lock is still important for proper running liquibase migrations,
 * there is only one way to implement it - another DB.
 * <p>
 * This specific implementation uses Redis as a DB to maintain distributed lock.
 */
public class ClickHouseLockService implements LockService {

    private static final int MILLIS_IN_SECOND = 1000;

    private Long changeLogLockWaitTime;
    private Long changeLogLocRecheckTime;
    private boolean hasChangeLogLock;

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof ClickHouseDatabase;
    }

    @Override
    public void setDatabase(Database database) {
        // No need to obtain ClickHouse DB since lock will be obtained with Redis
    }

    public Long getChangeLogLockWaitTime() {
        if (changeLogLockWaitTime != null) {
            return changeLogLockWaitTime;
        }
        return GlobalConfiguration.CHANGELOGLOCK_WAIT_TIME.getCurrentValue();
    }

    @Override
    public void setChangeLogLockWaitTime(long changeLogLockWaitTime) {
        this.changeLogLockWaitTime = changeLogLockWaitTime;
    }

    public Long getChangeLogLockRecheckTime() {
        if (changeLogLocRecheckTime != null) {
            return changeLogLocRecheckTime;
        }
        return GlobalConfiguration.CHANGELOGLOCK_POLL_RATE.getCurrentValue();
    }

    @Override
    public void setChangeLogLockRecheckTime(long changeLogLocRecheckTime) {
        this.changeLogLocRecheckTime = changeLogLocRecheckTime;
    }

    @Override
    public boolean hasChangeLogLock() {
        return hasChangeLogLock;
    }

    @Override
    public void waitForLock() throws LockException {
        boolean locked = false;
        val timeToGiveUp = Instant.now().plus(getChangeLogLockWaitTime(), ChronoUnit.MINUTES);
        while (!locked && Instant.now().isBefore(timeToGiveUp)) {
            locked = acquireLock();
            if (!locked) {
                getLogger().info("Waiting for changelog lock....");
                try {
                    Thread.sleep(getChangeLogLockRecheckTime() * MILLIS_IN_SECOND);
                } catch (InterruptedException e) {
                    // Restore thread interrupt status
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (!locked) {
            throw new LockException("Could not acquire change log lock");
        }
    }

    @Override
    public boolean acquireLock() {
        if (hasChangeLogLock) {
            return true;
        }
        if (RedisHelper.getRedisLockTemplate().opsForValue().setIfAbsent(getRedisKey(), getRedisKey()) == Boolean.TRUE) {
            hasChangeLogLock = true;
            return true;
        }
        return false;
    }

    @Override
    public void releaseLock() {
        RedisHelper.getRedisLockTemplate().delete(getRedisKey());
        hasChangeLogLock = false;
    }

    @Override
    public DatabaseChangeLogLock[] listLocks() {
        return new DatabaseChangeLogLock[0];
    }

    @Override
    public void forceReleaseLock() {
        releaseLock();
    }

    @Override
    public void reset() {
        releaseLock();
    }

    @Override
    public void init() {
        // Nothing to initialize ahead in Redis
    }

    @Override
    public void destroy() {
        releaseLock();
    }

    private Logger getLogger() {
        return Scope.getCurrentScope().getLog(ClickHouseLockService.class);
    }

    private String getRedisKey() {
        return "lock:clickhouse_liquibase_migration";
    }
}
