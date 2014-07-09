package com.viddu.handlebars;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Handlebars {

    private final ScriptEngine engine;
    
    private final ObjectMapper mapper;

    private final Logger logger = LoggerFactory.getLogger(Handlebars.class);

    public Handlebars(Reader reader) throws ScriptException {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        this.engine = engineManager.getEngineByName("nashorn");
        engine.eval(reader);
        engine.eval("var renderFunction = function(template, contextJSON){var context = JSON.parse(contextJSON); return Handlebars.compile(template)(context);}");
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
    }

    public Handlebars(InputStream is) throws ScriptException {
        this(new InputStreamReader(is));
    }

    public Handlebars(File file) throws FileNotFoundException, ScriptException {
        this(new FileReader(file));
    }

    public String render(String template, String contextJson) throws NoSuchMethodException, ScriptException {
        Invocable renderFunction = (Invocable) engine;
        String result = (String) renderFunction.invokeFunction("renderFunction", template, contextJson);
        logger.debug("Rendered Output:{}", result);
        return result;
    }

    public String render(String template, Object context) throws NoSuchMethodException, ScriptException,
            JsonProcessingException {
        String contextJson = mapper.writeValueAsString(context);
        logger.debug("Context JSON={}", contextJson);
        return render(template, contextJson);
    }

    public String render(InputStream is, Object context) throws NoSuchMethodException, JsonProcessingException,
            ScriptException {
        Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name());
        String template = scanner.useDelimiter("\\A").next();
        String renderedOutput = render(template, context);
        scanner.close();
        return renderedOutput;
    }
}
