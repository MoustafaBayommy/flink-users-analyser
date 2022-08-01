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

package goodWorkers;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.configuration.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MonthlyAnalyser {



    final static String bucketName= Optional.ofNullable(System.getenv("bucketName")).orElse("goodWorkers");
	final static String fileName= Optional.ofNullable(System.getenv("filePath")).orElse("usersEvents.csv");;
	final static String storagePath=String.format("s3://%s/%s",bucketName,fileName);
//	final static String storagePath="src/main/resources/userEvents.csv";// use that resource for validation

	final static String outputStreamName="users-logins-monthly";
	final static String awsRegion="us-east-1";
	final static String loggingEvent="LOGIN";

	final static ObjectMapper jsonParser = new ObjectMapper();

	public static void main(String[] args) throws Exception {
		Configuration flinkConfig = new Configuration();
		ExecutionEnvironment env = ExecutionEnvironment.createLocalEnvironmentWithWebUI(flinkConfig);
		env.setParallelism(6);

		DataSource<FlowLog> dataSource =env.readCsvFile(storagePath)
		    .ignoreFirstLine()
				.parseQuotedStrings('"')
				.ignoreInvalidLines()
				.pojoType(FlowLog.class, "userId","deviceId","clientId","clientVersion","sessionId","logType","eventName","log","timeStamp"
						);

		var monthAgoTimeStamp=OffsetDateTime.now().minusMonths(1).toEpochSecond();
		var nowTimeStamp=OffsetDateTime.now().toEpochSecond();


	var uniqueLoggedInUsers=dataSource
				.filter(v->(v.getTimeStamp()>=monthAgoTimeStamp)&&(v.getTimeStamp()<=nowTimeStamp))
			.filter(event->event.getEventName().equals(loggingEvent))
				.distinct(FlowLog::getUserId)
			.map(FlowLog::getUserId)
			.collect();

	publishResultToKinesis(uniqueLoggedInUsers,outputStreamName);

	}


	 static  void publishResultToKinesis(List<String> users, String topic){
		 System.out.println(users+  "publishing ####");

	 }






}
