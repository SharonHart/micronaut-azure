/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.azure.function.http;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.TraceContext;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.DefaultRequestBinderRegistry;
import io.micronaut.http.bind.binders.DefaultBodyAnnotationBinder;
import io.micronaut.http.bind.binders.RequestArgumentBinder;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.servlet.http.ServletBinderRegistry;

import io.micronaut.servlet.http.ServletBodyBinder;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Implementation of {@link ServletBinderRegistry} for Azure.
 *
 * @param <T> The body type
 *
 * @author graemerocher
 * @since 1.2.0
 */
@Singleton
@Replaces(DefaultRequestBinderRegistry.class)
@Internal
public class AzureBinderRegistry<T> extends ServletBinderRegistry<T> {

    private static final Argument<ExecutionContext> EXECUTION_CONTEXT_ARGUMENT = Argument.of(ExecutionContext.class);
    private static final Argument<TraceContext> TRACE_CONTEXT_ARGUMENT = Argument.of(TraceContext.class);
    private static final Argument<Logger> LOGGER_ARGUMENT = Argument.of(Logger.class);
    private static final Argument<HttpRequestMessage> REQUEST_MESSAGE_ARGUMENT = Argument.of(HttpRequestMessage.class);
    protected final DefaultBodyAnnotationBinder<T> defaultBodyAnnotationBinder;

    /**
     * Default constructor.
     *
     * @param mediaTypeCodecRegistry The media type codec registry
     * @param conversionService      The conversion service
     * @param binders                Any registered binders
     * @param defaultBodyAnnotationBinder The delegate default body binder
     */
    AzureBinderRegistry(
            MediaTypeCodecRegistry mediaTypeCodecRegistry,
            ConversionService conversionService,
            List<RequestArgumentBinder> binders,
            DefaultBodyAnnotationBinder<T> defaultBodyAnnotationBinder
    ) {
        super(mediaTypeCodecRegistry, conversionService, binders, defaultBodyAnnotationBinder);
        this.defaultBodyAnnotationBinder = defaultBodyAnnotationBinder;
        this.byType.put(HttpRequestMessage.class, new TypedRequestArgumentBinder<HttpRequestMessage>() {
            @Override
            public BindingResult<HttpRequestMessage> bind(
                    ArgumentConversionContext<HttpRequestMessage> context, HttpRequest<?> source) {
                if (source instanceof AzureFunctionHttpRequest) {
                    return () -> Optional.of(((AzureFunctionHttpRequest<?>) source).getNativeRequest());
                } else {
                    return BindingResult.EMPTY;
                }
            }

            @Override
            public Argument<HttpRequestMessage> argumentType() {
                return REQUEST_MESSAGE_ARGUMENT;
            }
        });
        this.byType.put(ExecutionContext.class, new TypedRequestArgumentBinder<ExecutionContext>() {
            @Override
            public BindingResult<ExecutionContext> bind(
                    ArgumentConversionContext<ExecutionContext> context, HttpRequest<?> source) {
                if (source instanceof AzureFunctionHttpRequest) {
                    return () -> Optional.of(((AzureFunctionHttpRequest<?>) source).getExecutionContext());
                } else {
                    return BindingResult.EMPTY;
                }
            }

            @Override
            public Argument<ExecutionContext> argumentType() {
                return EXECUTION_CONTEXT_ARGUMENT;
            }
        });
        this.byType.put(Logger.class, new TypedRequestArgumentBinder<Logger>() {
            @Override
            public BindingResult<Logger> bind(
                    ArgumentConversionContext<Logger> context, HttpRequest<?> source) {
                if (source instanceof AzureFunctionHttpRequest) {
                    return () -> Optional.of(((AzureFunctionHttpRequest<?>) source).getExecutionContext().getLogger());
                } else {
                    return BindingResult.EMPTY;
                }
            }

            @Override
            public Argument<Logger> argumentType() {
                return LOGGER_ARGUMENT;
            }
        });
        this.byType.put(TraceContext.class, new TypedRequestArgumentBinder<TraceContext>() {
            @Override
            public BindingResult<TraceContext> bind(
                    ArgumentConversionContext<TraceContext> context, HttpRequest<?> source) {
                if (source instanceof AzureFunctionHttpRequest) {
                    return () -> Optional.ofNullable(((AzureFunctionHttpRequest<?>) source).getExecutionContext().getTraceContext());
                } else {
                    return BindingResult.EMPTY;
                }
            }

            @Override
            public Argument<TraceContext> argumentType() {
                return TRACE_CONTEXT_ARGUMENT;
            }
        });
    }

    @Override
    protected ServletBodyBinder<T> newServletBodyBinder(
        MediaTypeCodecRegistry mediaTypeCodecRegistry,
        ConversionService conversionService,
        DefaultBodyAnnotationBinder<T> defaultBodyAnnotationBinder) {
        return new AzureServletBodyBinder<>(conversionService, mediaTypeCodecRegistry, defaultBodyAnnotationBinder);
    }
}
