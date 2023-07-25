/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.example.javaagent.instrumentation;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasClassesNamed;
import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.implementsInterface;
import static net.bytebuddy.matcher.ElementMatchers.*;

import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;

public class WebFluxInstrumentation implements TypeInstrumentation {
  @Override
  public ElementMatcher<ClassLoader> classLoaderOptimization() {
    return hasClassesNamed("org.springframework.web.reactive.HandlerAdapter");
  }

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return not(isAbstract())
        .and(implementsInterface(named("org.springframework.web.reactive.HandlerAdapter")));
  }

  @Override
  public void transform(TypeTransformer transformer) {
    transformer.applyAdviceToMethod(
        isMethod()
            .and(isPublic())
            .and(named("handle"))
            .and(takesArgument(0, named("org.springframework.web.server.ServerWebExchange")))
            .and(takesArgument(1, Object.class))
            .and(takesArguments(2)),
        "com.example.javaagent.instrumentation.ResponseTraceIdAdvice");
  }
}

@SuppressWarnings("unused")
class ResponseTraceIdAdvice {

  @Advice.OnMethodEnter(suppress = Throwable.class)
  public static void methodEnter(@Advice.Argument(0) ServerWebExchange exchange) {

    HttpHeaders headers = exchange.getResponse().getHeaders();
    String traceId = Java8BytecodeBridge.currentSpan().getSpanContext().getTraceId();
    if (!StringUtils.isNullOrEmpty(traceId)) {
      headers.add("x-trace-id", traceId);
    }
  }
}
