/*
 * Copyright 2024 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.adamcin.jardelta.testing.example.impl;

import net.adamcin.jardelta.testing.example.ExampleSingleton;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@org.osgi.service.component.annotations.Component(property = {
        "exampleSingleton.top.long1:Long=45"
})
@Designate(ocd = ExampleSingletonImpl.Config.class)
@Component(metatype = true)
@Service
@Properties({
        @Property(name = "exampleSingleton.top.long1", longValue = 45)
})
public class ExampleSingletonImpl implements ExampleSingleton {

    public static final String DEFAULT_CONST = "const value";

    @Property(value = DEFAULT_CONST, label = "Const String", description =  "Const String - description")
    public static final String PROP_CONST = "exampleSingleton.const.string";

    @ObjectClassDefinition
    public @interface Config {
        @AttributeDefinition(name = "Const String", description = "Const String - description",
                options = {@Option(DEFAULT_CONST)})
        String exampleSingleton_const_string() default DEFAULT_CONST;
    }

    @Reference(target = "(custom.name=foobar)")
    @org.osgi.service.component.annotations.Reference
    private Runnable runnableService;

    @Activate
    @org.osgi.service.component.annotations.Activate
    protected void activate() {
        /* do nothing */
    }
}
