/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sharding.checker;

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.config.rule.checker.RuleConfigurationCheckerFactory;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingRuleConfigurationCheckerTest {
    
    @Test
    public void assertCheckSuccess() {
        ShardingRuleConfiguration ruleConfig = createRuleConfiguration();
        ShardingAuditStrategyConfiguration shardingAuditStrategyConfig = new ShardingAuditStrategyConfiguration(Collections.singletonList("foo_audit"), false);
        ShardingStrategyConfiguration shardingStrategyConfig = createShardingStrategyConfiguration();
        ruleConfig.setTables(Collections.singleton(createShardingTableRuleConfiguration(shardingStrategyConfig, shardingAuditStrategyConfig, ruleConfig.getDefaultKeyGenerateStrategy())));
        ruleConfig.setAutoTables(Collections.singleton(createShardingAutoTableRuleConfiguration(shardingStrategyConfig, shardingAuditStrategyConfig, ruleConfig.getDefaultKeyGenerateStrategy())));
        getChecker(ruleConfig).check("foo_db", ruleConfig, Collections.emptyMap(), Collections.emptyList());
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertCheckTableConfigurationFailed() {
        ShardingRuleConfiguration configuration = createRuleConfiguration();
        configuration.setTables(Collections.singletonList(createShardingTableRuleConfiguration(null, null, null)));
        configuration.setAutoTables(Collections.singleton(createShardingAutoTableRuleConfiguration(null, null, null)));
        getChecker(configuration).check("foo_db", configuration, Collections.emptyMap(), Collections.emptyList());
    }
    
    private ShardingRuleConfiguration createRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getShardingAlgorithms().put("foo_algorithm", mock(ShardingSphereAlgorithmConfiguration.class));
        result.getAuditors().put("foo_audit", mock(ShardingSphereAlgorithmConfiguration.class));
        result.getKeyGenerators().put("foo_keygen", mock(ShardingSphereAlgorithmConfiguration.class));
        result.setDefaultKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("foo_col", "foo_keygen"));
        return result;
    }
    
    private ShardingStrategyConfiguration createShardingStrategyConfiguration() {
        ShardingStrategyConfiguration result = mock(ShardingStrategyConfiguration.class);
        when(result.getShardingAlgorithmName()).thenReturn("foo_algorithm");
        return result;
    }
    
    private ShardingTableRuleConfiguration createShardingTableRuleConfiguration(final ShardingStrategyConfiguration shardingStrategyConfig,
                                                                                final ShardingAuditStrategyConfiguration shardingAuditStrategyConfig,
                                                                                final KeyGenerateStrategyConfiguration keyGenerateStrategyConfig) {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("foo_tbl");
        result.setDatabaseShardingStrategy(null == shardingStrategyConfig ? mock(ShardingStrategyConfiguration.class) : shardingStrategyConfig);
        result.setTableShardingStrategy(null == shardingStrategyConfig ? mock(ShardingStrategyConfiguration.class) : shardingStrategyConfig);
        result.setAuditStrategy(null == shardingAuditStrategyConfig ? mock(ShardingAuditStrategyConfiguration.class) : shardingAuditStrategyConfig);
        result.setKeyGenerateStrategy(null == keyGenerateStrategyConfig ? mock(KeyGenerateStrategyConfiguration.class) : keyGenerateStrategyConfig);
        return result;
    }
    
    private ShardingAutoTableRuleConfiguration createShardingAutoTableRuleConfiguration(final ShardingStrategyConfiguration shardingStrategyConfig,
                                                                                        final ShardingAuditStrategyConfiguration shardingAuditStrategyConfig,
                                                                                        final KeyGenerateStrategyConfiguration keyGenerateStrategyConfig) {
        ShardingAutoTableRuleConfiguration result = mock(ShardingAutoTableRuleConfiguration.class);
        when(result.getShardingStrategy()).thenReturn(null == shardingStrategyConfig ? mock(ShardingStrategyConfiguration.class) : shardingStrategyConfig);
        when(result.getAuditStrategy()).thenReturn(null == shardingAuditStrategyConfig ? mock(ShardingAuditStrategyConfiguration.class) : shardingAuditStrategyConfig);
        when(result.getKeyGenerateStrategy()).thenReturn(null == keyGenerateStrategyConfig ? mock(KeyGenerateStrategyConfiguration.class) : keyGenerateStrategyConfig);
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private RuleConfigurationChecker<ShardingRuleConfiguration> getChecker(final ShardingRuleConfiguration ruleConfig) {
        Optional<RuleConfigurationChecker> result = RuleConfigurationCheckerFactory.findInstance(ruleConfig);
        assertTrue(result.isPresent());
        return result.get();
    }
}
