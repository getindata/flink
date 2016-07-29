/**
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
package org.apache.flink.yarn;

import akka.testkit.JavaTestKit;
import org.apache.curator.test.TestingServer;
import org.apache.flink.runtime.akka.AkkaUtils;
import org.apache.flink.runtime.security.SecurityContext;
import org.apache.flink.test.util.SecureTestEnvironment;
import org.apache.flink.test.util.TestingSecurityContext;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YARNHighAvailabilitySecuredITCase extends YARNHighAvailabilityITCase {

	protected static final Logger LOG = LoggerFactory.getLogger(YARNHighAvailabilitySecuredITCase.class);

	@BeforeClass
	public static void setup() {

		LOG.info("starting secure cluster environment for testing");

		yarnConfiguration.set(YarnTestBase.TEST_CLUSTER_NAME_KEY, "flink-yarn-tests-ha-secured");
		yarnConfiguration.set(YarnConfiguration.RM_AM_MAX_ATTEMPTS, "" + numberApplicationAttempts);

		SecureTestEnvironment.prepare(tmp);

		populateYarnSecureConfigurations(yarnConfiguration,SecureTestEnvironment.getHadoopServicePrincipal(),
				SecureTestEnvironment.getTestKeytab());

		SecurityContext.SecurityConfiguration ctx = new SecurityContext.SecurityConfiguration();
		ctx.setCredentials(SecureTestEnvironment.getTestKeytab(), SecureTestEnvironment.getHadoopServicePrincipal());
		ctx.setHadoopConfiguration(yarnConfiguration);
		try {
			TestingSecurityContext.install(ctx, SecureTestEnvironment.getClientSecurityConfigurationMap());
		} catch(Exception e) {
			throw new RuntimeException("Exception occurred while setting up secure test context. Reason: {}", e);
		}

		actorSystem = AkkaUtils.createDefaultActorSystem();

		try {
			zkServer = new TestingServer();
			zkServer.start();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Could not start ZooKeeper testing cluster.");
		}

		startYARNSecureMode(yarnConfiguration, SecureTestEnvironment.getHadoopServicePrincipal(),
				SecureTestEnvironment.getTestKeytab());
	}

	@AfterClass
	public static void teardown() throws Exception {

		LOG.info("tearing down secure cluster environment");

		if(zkServer != null) {
			zkServer.stop();
		}

		JavaTestKit.shutdownActorSystem(actorSystem);
		actorSystem = null;

		SecureTestEnvironment.cleanup();
	}
}