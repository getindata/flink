/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flink.metrics.groups;

import org.apache.flink.configuration.ConfigConstants;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.Gauge;
import org.apache.flink.metrics.Histogram;
import org.apache.flink.metrics.HistogramStatistics;
import org.apache.flink.metrics.Metric;
import org.apache.flink.metrics.MetricGroup;
import org.apache.flink.metrics.MetricRegistry;
import org.apache.flink.metrics.util.TestReporter;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class MetricGroupRegistrationTest {
	/**
	 * Verifies that group methods instantiate the correct metric with the given name.
	 */
	@Test
	public void testMetricInstantiation() {
		Configuration config = new Configuration();
		config.setString(ConfigConstants.METRICS_REPORTER_CLASS, TestReporter1.class.getName());

		MetricRegistry registry = new MetricRegistry(config);

		MetricGroup root = new TaskManagerMetricGroup(registry, "host", "id");

		Counter counter = root.counter("counter");
		assertEquals(counter, TestReporter1.lastPassedMetric);
		assertEquals("counter", TestReporter1.lastPassedName);

		Gauge<Object> gauge = root.gauge("gauge", new Gauge<Object>() {
			@Override
			public Object getValue() {
				return null;
			}
		});
		
		Assert.assertEquals(gauge, TestReporter1.lastPassedMetric);
		assertEquals("gauge", TestReporter1.lastPassedName);

		Histogram histogram = root.histogram("histogram", new Histogram() {
			@Override
			public void update(long value) {

			}

			@Override
			public long getCount() {
				return 0;
			}

			@Override
			public HistogramStatistics getStatistics() {
				return null;
			}
		});

		Assert.assertEquals(histogram, TestReporter1.lastPassedMetric);
		assertEquals("histogram", TestReporter1.lastPassedName);
		registry.shutdown();
	}

	public static class TestReporter1 extends TestReporter {
		
		public static Metric lastPassedMetric;
		public static String lastPassedName;

		@Override
		public void notifyOfAddedMetric(Metric metric, String metricName, AbstractMetricGroup group) {
			lastPassedMetric = metric;
			lastPassedName = metricName;
		}
	}

	/**
	 * Verifies that when attempting to create a group with the name of an existing one the existing one will be returned instead.
	 */
	@Test
	public void testDuplicateGroupName() {
		Configuration config = new Configuration();

		MetricRegistry registry = new MetricRegistry(config);

		MetricGroup root = new TaskManagerMetricGroup(registry, "host", "id");

		MetricGroup group1 = root.addGroup("group");
		MetricGroup group2 = root.addGroup("group");
		MetricGroup group3 = root.addGroup("group");
		Assert.assertTrue(group1 == group2 && group2 == group3);

	}
}
