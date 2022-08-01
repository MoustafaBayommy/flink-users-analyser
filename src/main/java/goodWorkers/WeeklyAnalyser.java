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

import java.util.Objects;
import java.util.Properties;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.kinesis.shaded.com.amazonaws.SDKGlobalConfiguration;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kinesis.FlinkKinesisConsumer;
import org.apache.flink.streaming.connectors.kinesis.FlinkKinesisProducer;
import org.apache.flink.streaming.connectors.kinesis.config.ConsumerConfigConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WeeklyAnalyser {

	final static String userEventsStream = "usersFlow";
	final static String outputStreamName = "users-logins-weekly";
	final static String awsRegion = "us-east-1";
	final static String loggingEvent = "LOGIN";

	final static ObjectMapper jsonParser = new ObjectMapper();

	public static void main(String[] args) throws Exception {
		// REQUIRED SETTINGS FOR LOCAL KINESIS
		System.setProperty(SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY, "true");
		System.setProperty("org.apache.flink.kinesis.shaded.com.amazonaws.sdk.disableCertChecking", "true");
		// set up the streaming execution environment
		Configuration flinkConfig = new Configuration();
		StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(flinkConfig);
		env.enableCheckpointing(60000, CheckpointingMode.EXACTLY_ONCE);
		env.setParallelism(6);

		// set up kinesis consumer pointing to local kinesis instance
		Properties kinesisProps = new Properties();
		kinesisProps.setProperty(ConsumerConfigConstants.AWS_REGION, awsRegion);
		kinesisProps.put(ConsumerConfigConstants.AWS_ACCESS_KEY_ID, "fake_access_key");
		kinesisProps.put(ConsumerConfigConstants.AWS_SECRET_ACCESS_KEY, "fake_secret_access_key");
		kinesisProps.setProperty(ConsumerConfigConstants.AWS_ENDPOINT, "https://localhost:4567");
		kinesisProps.setProperty(ConsumerConfigConstants.STREAM_INITIAL_POSITION, "TRIM_HORIZON");

		DataStreamSource<String> stringSource = env.addSource(new FlinkKinesisConsumer(userEventsStream, new SimpleStringSchema(), kinesisProps));

		stringSource
				.map(value -> {
					JsonNode jsonNode = jsonParser.readValue(value, JsonNode.class);
					return new Tuple2<>(jsonNode.get("userId").toString(), jsonNode.get("eventName").toString());
				}).returns(Types.TUPLE(Types.STRING, Types.STRING))
				.filter(v -> v.f1.equals(loggingEvent))
				.keyBy(v -> v.f0)
				.window(TumblingEventTimeWindows.of(Time.days(7)))
				.aggregate(new UniqueUserLogsAgg())
				.filter(Objects::nonNull)
				.addSink(createSinkFromStaticConfig());

		// execute program
		env.execute("userEvents Analyser");
	}


	private static FlinkKinesisProducer<String> createSinkFromStaticConfig() {
		Properties outputProperties = new Properties();
		outputProperties.setProperty(ConsumerConfigConstants.AWS_REGION, awsRegion);

		FlinkKinesisProducer<String> sink = new FlinkKinesisProducer<>(new
				SimpleStringSchema(), outputProperties);
		sink.setDefaultStream(outputStreamName);
		sink.setDefaultPartition("0");
		return sink;
	}


	private static class UniqueUserLogsAgg implements AggregateFunction<Tuple2<String, String>, Tuple2<String, Integer>, String> {

		@Override
		public Tuple2<String, Integer> createAccumulator() {
			return Tuple2.of(null, 0);
		}

		@Override
		public Tuple2<String, Integer> add(Tuple2<String, String> value, Tuple2<String, Integer> accumulator) {
			return Tuple2.of(value.f0, accumulator.f1 + 1);
		}

		@Override
		public String getResult(Tuple2<String, Integer> accumulator) {
			return accumulator.f0;
		}

		@Override
		public Tuple2<String, Integer> merge(Tuple2<String, Integer> a, Tuple2<String, Integer> b) {
			return Tuple2.of(a.f0, a.f1 + b.f1);
		}
	}


}
