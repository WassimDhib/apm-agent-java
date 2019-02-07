/*-
 * #%L
 * Elastic APM Java agent
 * %%
 * Copyright (C) 2018 Elastic and contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package co.elastic.apm.profiler;

import co.elastic.apm.bci.ElasticApmInstrumentation;
import co.elastic.apm.bci.VisibleForAdvice;
import co.elastic.apm.impl.ElasticApmTracer;
import co.elastic.apm.impl.stacktrace.StacktraceConfiguration;
import co.elastic.apm.impl.transaction.AbstractSpan;
import co.elastic.apm.impl.transaction.Span;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

import static co.elastic.apm.bci.bytebuddy.CustomElementMatchers.isInAnyPackage;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Creates spans for Method calls
 */
public class MethodTimingInstrumentation extends ElasticApmInstrumentation {

    static final String PROFILER_INSTRUMENTATION_GROUP = "profiler";
    private Collection<String> applicationPackages = Collections.emptyList();


    @Nullable
    @VisibleForAdvice
    @Advice.OnMethodEnter
    public static Span onBeforeExecute(@Advice.Origin("#m") String methodName) {
    	 if (tracer != null) {
    		 final AbstractSpan<?> parent = (AbstractSpan<?>) tracer.getActive();

    		 if ( parent == null || !parent.isSampled()) {
    	            return null;
    	        }
    	        Span span = parent.createSpan().activate();
    	        span.setName(methodName);
    	        span.withType("profiler");
    	     
    	        return span;
    	        
         }
    	 
    	 return null;
    }


    @Override
    public void init(ElasticApmTracer tracer) {
        applicationPackages = tracer.getConfig(StacktraceConfiguration.class).getApplicationPackages();
    }


    @VisibleForAdvice
    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onAfterExecute(@Advice.Enter @Nullable Span span, @Advice.Thrown Throwable t) {
        if (span != null) {
            span.captureException(t)
                .deactivate()
                .end();
        }
    }


    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {

        return not(isInterface())
        	.and(isInAnyPackage(applicationPackages, ElementMatchers.<NamedElement>any()))
            .and(not(nameContains("org.aspectj.")))
            .and(not(nameContains("org.groovy.")))
            .and(not(nameContains("com.p6spy.")))
            .and(not(nameContains("net.bytebuddy.")))
            .and(not(nameContains("javassist")))
            .and(not(nameContains(".asm.")))
        	.and(not(nameContainsIgnoreCase("cglib")));

    }

    @Override
    public ElementMatcher<? super MethodDescription> getMethodMatcher() {
        return  not(isConstructor())
				.and(not(isAbstract()))
				.and(not(isNative()))
				.and(not(isFinal()))
				.and(not(isSynthetic()))
				.and(not(isTypeInitializer()));
    }

    

    @Override
    public Collection<String> getInstrumentationGroupNames() {
        return Collections.singleton(PROFILER_INSTRUMENTATION_GROUP);
    }
}
