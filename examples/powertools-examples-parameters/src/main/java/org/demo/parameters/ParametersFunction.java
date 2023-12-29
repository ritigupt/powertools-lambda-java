/*
 * Copyright 2023 Amazon.com, Inc. or its affiliates.
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.demo.parameters;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static software.amazon.lambda.powertools.parameters.transform.Transformer.base64;
import static software.amazon.lambda.powertools.parameters.transform.Transformer.json;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.lambda.powertools.parameters.secrets.SecretsParam;
import software.amazon.lambda.powertools.parameters.secrets.SecretsProvider;
import software.amazon.lambda.powertools.parameters.ssm.SSMParam;
import software.amazon.lambda.powertools.parameters.ssm.SSMProvider;
import software.amazon.lambda.powertools.parameters.transform.TransformationManager;

public class ParametersFunction implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger log = LoggerFactory.getLogger(ParametersFunction.class);

    // Annotation-style injection from secrets manager
    @SecretsParam(key = "/powertools-java/userpwd")
    String secretParamInjected;

    // Annotation-style injection from Systems Manager
    @SSMParam(key = "/powertools-java/sample/simplekey")
    String ssmParamInjected;

    SSMProvider ssmProvider = SSMProvider
            .builder()
            .withTransformationManager(new TransformationManager())
            .build();
    SecretsProvider secretsProvider = SecretsProvider
            .builder()
            .withTransformationManager(new TransformationManager())
            .build();


    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

        String simpleValue = ssmProvider.withMaxAge(1, MILLIS).get("/powertools-java/sample/simplekey");
        String listValue = ssmProvider.withMaxAge(1, MILLIS).get("/powertools-java/sample/keylist");
        String jsonObj = ssmProvider.withMaxAge(1, MILLIS).get("/powertools-java/sample/keyjson");
        Map<String, String> allValues = ssmProvider.withMaxAge(1, MILLIS).getMultiple("/powertools-java/sample");


        log.info("allvalues (multiple):");
        allValues.forEach((key, value) -> log.info("- {}={}\n", key, value));

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);

        //final String pageContents = this.getPageContents("https://checkip.amazonaws.com");
        String output = String.format("{ \"message\": \"hello world\", \"location\": \"%s\" }", "bloop");

        return response
                .withStatusCode(200)
                .withBody(output);

    }

    private String getPageContents(String address) throws IOException {
        URL url = new URL(address);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
}

