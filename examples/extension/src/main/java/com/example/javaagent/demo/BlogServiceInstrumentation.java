/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.example.javaagent.demo;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.namedOneOf;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * The type Blog service instrumentation.
 *
 * @author pengfei.lu
 */
public class BlogServiceInstrumentation implements TypeInstrumentation {
  /**
   * Type matcher element matcher.
   *
   * @return the element matcher
   */
  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return named("com.alex.service.BlogService");
  }

  /**
   * Transform.
   *
   * @param typeTransformer the type transformer
   */
  @Override
  public void transform(TypeTransformer typeTransformer) {
    typeTransformer.applyAdviceToMethod(
        namedOneOf("getPublicBlogs")
            .and(ElementMatchers.takesArgument(0, ElementMatchers.named("java.lang.Integer")))
            .and(ElementMatchers.takesArgument(1, ElementMatchers.named("java.lang.Integer")))
            .and(ElementMatchers.isPublic()),
        this.getClass().getName() + "$BlogServiceAdvice");
  }

  /**
   * The type Blog service advice.
   *
   * @author pengfei.lu
   */
  @SuppressWarnings("unused")
  public static class BlogServiceAdvice {

    /**
     * On enter.
     *
     * @param pageSize the page size
     */
    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void onEnter(@Advice.Argument(value = 1) Integer pageSize) {
      System.out.println("BlogService advice, pageSize: " + pageSize);

      Tracer tracer = GlobalOpenTelemetry.getTracer("alex-service-instrumented-library", "0.0.1");
      Span span = tracer.spanBuilder("custom span").setSpanKind(SpanKind.CLIENT).startSpan();

      // Make the span the current span
      try (Scope ss = span.makeCurrent()) {
        // In this scope, the span is the current/active span
        span.setAttribute("hello", "world");
      } finally {
        span.end();
      }
    }
  }
}
