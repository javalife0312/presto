/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.sql.planner.iterative.rule;

import com.facebook.presto.sql.planner.assertions.PlanMatchPattern;
import com.facebook.presto.sql.planner.iterative.rule.test.RuleTester;
import com.facebook.presto.sql.planner.plan.Assignments;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.facebook.presto.spi.type.BigintType.BIGINT;
import static com.facebook.presto.sql.planner.assertions.PlanMatchPattern.project;
import static com.facebook.presto.sql.planner.assertions.PlanMatchPattern.values;
import static com.facebook.presto.sql.planner.iterative.rule.test.PlanBuilder.expression;
import static io.airlift.testing.Closeables.closeAllRuntimeException;

public class TestPruneValuesColumns
{
    private RuleTester tester;

    @BeforeClass
    public void setUp()
    {
        tester = new RuleTester();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown()
    {
        closeAllRuntimeException(tester);
        tester = null;
    }

    @Test
    public void testNotAllOutputsReferenced()
            throws Exception
    {
        tester.assertThat(new PruneValuesColumns())
                .on(p ->
                        p.project(
                                Assignments.of(p.symbol("y", BIGINT), expression("x")),
                                p.values(
                                        ImmutableList.of(p.symbol("unused", BIGINT), p.symbol("x", BIGINT)),
                                        ImmutableList.of(
                                                ImmutableList.of(expression("1"), expression("2")),
                                                ImmutableList.of(expression("3"), expression("4"))))))
                .matches(
                        project(
                                ImmutableMap.of("y", PlanMatchPattern.expression("x")),
                                values(
                                        ImmutableList.of("x"),
                                        ImmutableList.of(
                                                ImmutableList.of(expression("2")),
                                                ImmutableList.of(expression("4"))))));
    }

    @Test
    public void testAllOutputsReferenced()
            throws Exception
    {
        tester.assertThat(new PruneValuesColumns())
                .on(p ->
                        p.project(
                                Assignments.of(p.symbol("y", BIGINT), expression("x")),
                                p.values(p.symbol("x", BIGINT))))
                .doesNotFire();
    }
}