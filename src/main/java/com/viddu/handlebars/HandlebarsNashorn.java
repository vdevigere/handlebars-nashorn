package com.viddu.handlebars;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Scanner;
import java.util.function.BiFunction;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class HandlebarsNashorn implements Handlebars {
    private final ScriptEngine engine;
    private final ObjectMapper objectMapper, mapper;
    private final Object handlebars;
    private final Object JSON;

    private final Logger logger = LoggerFactory.getLogger(HandlebarsNashorn.class);

    HandlebarsNashorn(Reader handlebarsLib) throws HandlebarsException {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        this.engine = engineManager.getEngineByName("nashorn");
        try {
            engine.eval(handlebarsLib);
            handlebars = engine.eval("Handlebars");
            JSON = engine.eval("JSON");

            engine.eval("var ScriptUtils = Java.type('jdk.nashorn.api.scripting.ScriptUtils')\n" + 
                    "var wrapper = function(name, biFunctionInstance){\n" + 
                    "    Handlebars.registerHelper(name, function(context, options){\n" + 
                    "        var wrappedContext = ScriptUtils.wrap(context);\n" + 
                    "        var wrappedOptions = ScriptUtils.wrap(options);\n" + 
                    "        return biFunctionInstance.apply(wrappedContext, wrappedOptions); \n" + 
                    "    }); \n" + 
                    "}");
        } catch (ScriptException e) {
            logger.debug("Error in script:{}", e);
            throw new HandlebarsException(e);
        }
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        this.mapper = new ObjectMapper();
    }

    @Override
    public String render(String templateString, String contextJson) throws HandlebarsException {
        logger.debug("Context JSON={}", contextJson);
        logger.debug("templateString={}", templateString);
        Invocable render = (Invocable) engine;
        try {
            Object context = render.invokeMethod(JSON, "parse", contextJson);
            ScriptObjectMirror obj = (ScriptObjectMirror) render.invokeMethod(handlebars, "compile", templateString);
            return (String) obj.call(null, context);
        } catch (NoSuchMethodException | ScriptException e) {
            throw new HandlebarsException(e);
        }
    }

    @Override
    public String render(String templateString, Map<String, Object> context) throws HandlebarsException {
        String contextJson = null;
        try {
            contextJson = mapper.writeValueAsString(context);
        } catch (JsonProcessingException e) {
            throw new HandlebarsException(e);
        }
        return render(templateString, contextJson);
    }

    @Override
    public String render(String templateString, Collection<?> context) throws HandlebarsException {
        String contextJson = null;
        try {
            contextJson = mapper.writeValueAsString(context);
        } catch (JsonProcessingException e) {
            throw new HandlebarsException(e);
        }
        return render(templateString, contextJson);
    }

    @Override
    public String render(String templateString, Object context) throws HandlebarsException {
        String contextJson = null;
        try {
            contextJson = objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException e) {
            throw new HandlebarsException(e);
        }
        return render(templateString, contextJson);
    }

    @Override
    public String render(InputStream templateStream, String contextJson) throws HandlebarsException {
        Scanner scanner = new Scanner(templateStream, StandardCharsets.UTF_8.name());
        String template = scanner.useDelimiter("\\A").next();
        return render(template, contextJson);
    }

    @Override
    public String render(InputStream templateStream, Map<String, Object> context) throws HandlebarsException {
        Scanner scanner = new Scanner(templateStream, StandardCharsets.UTF_8.name());
        String template = scanner.useDelimiter("\\A").next();
        return render(template, context);
    }

    @Override
    public String render(InputStream templateStream, Collection<?> context) throws HandlebarsException {
        Scanner scanner = new Scanner(templateStream, StandardCharsets.UTF_8.name());
        String template = scanner.useDelimiter("\\A").next();
        return render(template, context);
    }

    @Override
    public String render(InputStream templateStream, Object context) throws HandlebarsException {
        Scanner scanner = new Scanner(templateStream, StandardCharsets.UTF_8.name());
        String template = scanner.useDelimiter("\\A").next();
        return render(template, context);
    }

    @Override
    public <T, U> void registerHelper(String helperName, BiFunction<ScriptObjectMirror, ScriptObjectMirror, String> biFunction)
            throws HandlebarsException {
        Invocable invocable = (Invocable) engine;
        try {
            invocable.invokeFunction("wrapper", helperName, biFunction);
        } catch (NoSuchMethodException | ScriptException e) {
            throw new HandlebarsException(e);
        }
    }

}
