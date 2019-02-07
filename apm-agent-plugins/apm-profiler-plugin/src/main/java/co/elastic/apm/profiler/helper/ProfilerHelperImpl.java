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
package co.elastic.apm.profiler.helper;

import co.elastic.apm.impl.ElasticApmTracer;
import co.elastic.apm.impl.transaction.AbstractSpan;
import co.elastic.apm.impl.transaction.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class ProfilerHelperImpl implements ProfilerHelper {

    private static final Logger logger = LoggerFactory.getLogger(ProfilerHelperImpl.class);
  
    @Override
    @Nullable
    public Span createMethodSpan(@Nullable String methodName, @Nullable AbstractSpan<?> parent) {
        if (methodName == null || parent == null || !parent.isSampled()) {
            return null;
        }
        Span span = parent.createSpan().activate();
        span.setName(methodName);
        span.withType("method.execution");
     
        return span;
    }

}
