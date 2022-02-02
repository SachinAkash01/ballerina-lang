/*
*  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.test.expressions.object;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.JvmRunUtil;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for service-constructor expression in ballerina.
 */
@Test
public class ServiceConstructorTest {

    private CompileResult compiledConstructedObjects;

    @BeforeClass
    public void setup() {
        compiledConstructedObjects = BCompileUtil.compile(
                "test-src/expressions/object/service_constructor_expression.bal");
    }

    @Test
    public void testObjectCreationViaObjectConstructor() {
        JvmRunUtil.invoke(compiledConstructedObjects, "testServiceCtor");
    }

    @AfterClass
    public void tearDown() {
        compiledConstructedObjects = null;
    }
}