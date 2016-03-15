package com.github.dyna4jdbc.internal.scriptengine.jdbc.impl;

import com.github.dyna4jdbc.internal.ClosableSQLObject;
import com.github.dyna4jdbc.internal.common.jdbc.base.AbstractConnection;
import com.github.dyna4jdbc.internal.common.outputhandler.impl.DefaultScriptOutputHandlerFactory;

import javax.script.*;
import java.io.Writer;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Executor;

public class ScriptEngineConnection extends AbstractConnection {

    final String engineName;

    private final String configuration;
    private final Properties properties;

    private final ScriptEngine engine;

    public ScriptEngineConnection(String parameters, Properties properties)
    {
        this.properties = properties;

        if(parameters == null || "".equals(parameters.trim())) {
            throw new IllegalArgumentException("Scrip Engine Name not specified");
        }

        String[] engineNameAndParameters = parameters.split(":", 2);

        engineName = engineNameAndParameters[0];
        configuration = engineNameAndParameters.length == 2 ? engineNameAndParameters[1] : null;

        if(engineName == null || "".equals(engineName.trim())) {
            throw new IllegalArgumentException("Scrip Engine Name not specified");
        }

        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        this.engine = scriptEngineManager.getEngineByName(engineName);
        if(this.engine == null) {
            throw new IllegalArgumentException("ScriptEngine not found: " + engineName);
        }

    }

    public DatabaseMetaData getMetaData() throws SQLException {
        checkNotClosed();
        return new ScriptEngineDatabaseMetaData(this);
    }

    public Statement createStatement() throws SQLException {
        checkNotClosed();
        return new ScriptEngineStatement(this, new DefaultScriptOutputHandlerFactory());
    }


    interface ScriptEngineCallback<T> {
        T execute(ScriptEngine engine) throws ScriptException;
    }

    <T> T executeUsingScriptEngine(ScriptEngineCallback<T> scriptEngineCallback) throws ScriptException {

        synchronized (engine) {
            Writer originalWriter = engine.getContext().getWriter();
            Writer originalErrorWriter = engine.getContext().getErrorWriter();

            try {

                return scriptEngineCallback.execute(engine);
            }
            finally {
                engine.getContext().setWriter(originalWriter);
                engine.getContext().setErrorWriter(originalErrorWriter);
            }
        }
    }


    public String getEngineDescription() {
        ScriptEngineFactory factory = engine.getFactory();
        if(factory != null) {
            String engineName = factory.getEngineName();
            String languageVersion = factory.getLanguageVersion();

            return String.format("%s (%s)", engineName, languageVersion);
        }

        return null;
    }

    public String getEngineVersion() {

        ScriptEngineFactory factory = engine.getFactory();
        if(factory != null) {
            return factory.getEngineVersion();
        }

        return null;
    }
}
