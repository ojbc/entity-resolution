/*
 * Copyright 2013 SEARCH Group, Incorporated. 
 * 
 * See the NOTICE file distributed with  this work for additional information 
 * regarding copyright ownership.  SEARCH Group Inc. licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not use this 
 * file except in compliance with the License.  You may obtain a copy of the 
 * License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.nij.camel.xpath;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.camel.language.xpath.XPathAnnotationExpressionFactory;
import org.apache.camel.support.language.LanguageAnnotation;
import org.apache.camel.support.language.NamespacePrefix;
import org.w3c.dom.NodeList;

/**
 * This class allows Camel to use parameter binding with namespace support
 * in message processors.  See this link for more information:
 * 
 * http://camel.apache.org/parameter-binding-annotations.html
 * 
 * 
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@LanguageAnnotation(language = "xpath", factory = XPathAnnotationExpressionFactory.class)
public @interface CamelXpathAnnotations {
    String value();

    // You can add the namespaces as the default value of the annotation
    NamespacePrefix[] namespaces() default {
    @NamespacePrefix(prefix = "merge", uri = "http://nij.gov/IEPD/Exchange/EntityMergeRequestMessage/1.0"),
    @NamespacePrefix(prefix = "er-ext", uri = "http://nij.gov/IEPD/Extensions/EntityResolutionExtensions/1.0")
    };

    Class<?> resultType() default NodeList.class;
}
