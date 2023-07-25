/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.example.javaagent.demo;

import static java.util.Collections.singletonList;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers;
import java.util.List;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * This is a demo instrumentation which hooks into servlet invocation and modifies the http
 * response.
 *
 * @author pengfei.lu
 */
@AutoService(InstrumentationModule.class)
public final class BlogServiceInstrumentationModule extends InstrumentationModule {
  /** Instantiates a new Blog service instrumentation module. */
  public BlogServiceInstrumentationModule() {
    super("alex-service", "alex-service-instrumented-library");
  }

  /**
   * We want this instrumentation to be applied after the standard servlet instrumentation. The
   * latter creates a server span around http request. This instrumentation needs access to that
   * server span.
   */
  @Override
  public int order() {
    return 1;
  }

  /**
   * Class loader matcher element matcher . junction.
   *
   * @return the element matcher . junction
   */
  @Override
  public ElementMatcher.Junction<ClassLoader> classLoaderMatcher() {
    return AgentElementMatchers.hasClassesNamed("com.alex.service.BlogService");
  }

  /**
   * Type instrumentations list.
   *
   * @return the list
   */
  @Override
  public List<TypeInstrumentation> typeInstrumentations() {
    return singletonList(new BlogServiceInstrumentation());
  }
}
