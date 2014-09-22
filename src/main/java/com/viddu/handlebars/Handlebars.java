/*
 * Copyright [Sep 22, 2014] [Viddu Devigere]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.viddu.handlebars;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

public interface Handlebars {

    public abstract String render(String templateString, String contextJson) throws HandlebarsException;

    public abstract String render(String templateString, Map<String, Object> context) throws HandlebarsException;

    public abstract String render(String templateString, Collection<?> context) throws HandlebarsException;

    public abstract String render(String templateString, Object context) throws HandlebarsException;

    public abstract String render(InputStream template, String contextJson) throws HandlebarsException;

    public abstract String render(InputStream template, Map<String, Object> context) throws HandlebarsException;

    public abstract String render(InputStream template, Collection<?> context) throws HandlebarsException;

    public abstract String render(InputStream template, Object context) throws HandlebarsException;

    public abstract <T, U> void registerHelper(String helperName, BiFunction<ScriptObjectMirror, ScriptObjectMirror, String> biFunction) throws HandlebarsException;
}