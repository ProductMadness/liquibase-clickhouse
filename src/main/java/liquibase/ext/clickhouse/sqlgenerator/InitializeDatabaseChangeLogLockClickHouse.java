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
package liquibase.ext.clickhouse.sqlgenerator;

import liquibase.database.Database;
import liquibase.ext.clickhouse.database.ClickHouseDatabase;
import liquibase.ext.clickhouse.params.ClusterConfig;
import liquibase.ext.clickhouse.params.ParamsLoader;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.InitializeDatabaseChangeLogLockTableGenerator;
import liquibase.statement.core.InitializeDatabaseChangeLogLockTableStatement;

public class InitializeDatabaseChangeLogLockClickHouse
        extends InitializeDatabaseChangeLogLockTableGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(
            InitializeDatabaseChangeLogLockTableStatement statement, Database database) {
        return database instanceof ClickHouseDatabase;
    }

    @Override
    public Sql[] generateSql(
            InitializeDatabaseChangeLogLockTableStatement statement,
            Database database,
            SqlGeneratorChain sqlGeneratorChain) {
        ClusterConfig properties = ParamsLoader.getLiquibaseClickhouseProperties();

        String clearDatabaseQuery =
                String.format(
                        "ALTER TABLE %s "
                                + SqlGeneratorUtil.generateSqlOnClusterClause(properties)
                                + "DELETE WHERE 1 SETTINGS mutations_sync = 2",
                        database.getDatabaseChangeLogLockTableName());

        String initLockQuery =
                String.format(
                        "INSERT INTO %s (ID, LOCKED) VALUES (1, 0)",
                        database.getDatabaseChangeLogLockTableName());

        return SqlGeneratorUtil.generateSql(database, clearDatabaseQuery, initLockQuery);
    }
}
