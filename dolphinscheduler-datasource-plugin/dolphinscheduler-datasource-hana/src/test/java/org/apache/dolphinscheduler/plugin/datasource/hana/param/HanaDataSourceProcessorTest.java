/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.plugin.datasource.hana.param;

import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HanaDataSourceProcessorTest {

    private HanaDataSourceProcessor hanaDataSourceProcessor = new HanaDataSourceProcessor();

    @Test
    void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        HanaDataSourceParamDTO mysqlDatasourceParamDTO = new HanaDataSourceParamDTO();
        mysqlDatasourceParamDTO.setUserName("root");
        mysqlDatasourceParamDTO.setPassword("123456");
        mysqlDatasourceParamDTO.setHost("localhost");
        mysqlDatasourceParamDTO.setPort(30015);
        mysqlDatasourceParamDTO.setDatabase("default");
        mysqlDatasourceParamDTO.setOther(props);
        try (MockedStatic<PasswordUtils> mockedPasswordUtils = Mockito.mockStatic(PasswordUtils.class)) {
            Mockito.when(PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
            HanaConnectionParam connectionParams = (HanaConnectionParam) hanaDataSourceProcessor
                    .createConnectionParams(mysqlDatasourceParamDTO);
            Assertions.assertEquals("jdbc:sap://localhost:30015", connectionParams.getAddress());
            Assertions.assertEquals("jdbc:sap://localhost:30015?currentschema=default", connectionParams.getJdbcUrl());
        }
    }

    @Test
    void testCreateConnectionParams2() {
        String connectionJson = "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:sap://localhost:30015\""
                + ",\"database\":\"default\",\"jdbcUrl\":\"jdbc:sap://localhost:30015?currentschema=default\"}";
        HanaConnectionParam connectionParams = (HanaConnectionParam) hanaDataSourceProcessor
                .createConnectionParams(connectionJson);
        Assertions.assertNotNull(connectionJson);
        Assertions.assertEquals("root", connectionParams.getUser());
    }

    @Test
    public void testGetJdbcUrl() {
        HanaConnectionParam hanaConnectionParam = new HanaConnectionParam();
        hanaConnectionParam.setJdbcUrl("jdbc:sap://localhost:30015?currentschema=default");
        Assertions.assertEquals(
                "jdbc:sap://localhost:30015?currentschema=default&reconnect=true",
                hanaDataSourceProcessor.getJdbcUrl(hanaConnectionParam));
    }

    @Test
    void testGetDbType() {
        Assertions.assertEquals(DbType.HANA, hanaDataSourceProcessor.getDbType());
    }

    @Test
    void testGetDatasourceUniqueId() {
        HanaConnectionParam mysqlConnectionParam = new HanaConnectionParam();
        mysqlConnectionParam.setJdbcUrl("jdbc:sap://localhost:30015?currentschema=default");
        mysqlConnectionParam.setUser("root");
        mysqlConnectionParam.setPassword("123456");
        try (MockedStatic<PasswordUtils> mockedPasswordUtils = Mockito.mockStatic(PasswordUtils.class)) {
            Mockito.when(PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("123456");
            Assertions.assertEquals("hana@root@123456@jdbc:sap://localhost:30015?currentschema=default",
                    hanaDataSourceProcessor.getDatasourceUniqueId(mysqlConnectionParam, DbType.HANA));
        }
    }
}
