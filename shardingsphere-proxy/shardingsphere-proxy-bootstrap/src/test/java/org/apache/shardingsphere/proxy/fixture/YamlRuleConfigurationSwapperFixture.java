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

package org.apache.shardingsphere.proxy.fixture;

import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.test.fixture.rule.MockedRuleConfiguration;

public final class YamlRuleConfigurationSwapperFixture implements YamlRuleConfigurationSwapper<YamlRuleConfigurationFixture, MockedRuleConfiguration> {
    
    @Override
    public Class<MockedRuleConfiguration> getTypeClass() {
        return MockedRuleConfiguration.class;
    }
    
    @Override
    public YamlRuleConfigurationFixture swapToYamlConfiguration(final MockedRuleConfiguration data) {
        YamlRuleConfigurationFixture result = new YamlRuleConfigurationFixture();
        result.setName(data.getName());
        return result;
    }
    
    @Override
    public MockedRuleConfiguration swapToObject(final YamlRuleConfigurationFixture yamlConfig) {
        return new MockedRuleConfiguration(yamlConfig.getName());
    }
    
    @Override
    public String getRuleTagName() {
        return "FIXTURE";
    }
    
    @Override
    public int getOrder() {
        return -1;
    }
}
