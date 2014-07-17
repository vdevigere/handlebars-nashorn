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