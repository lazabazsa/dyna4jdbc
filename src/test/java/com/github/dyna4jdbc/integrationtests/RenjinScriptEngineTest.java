package com.github.dyna4jdbc.integrationtests;

import org.testng.annotations.Test;

import java.sql.SQLException;

public class RenjinScriptEngineTest extends AbstractScriptEngineIntegrationTest {

    protected RenjinScriptEngineTest() {
        super("jdbc:dyna4jdbc:scriptengine:Renjin");
    }

    @Test
    public void testWritingFromUpdateThrowsSQLException() {

        String script = "cat('Hello World') ";

        assertWritingFromUpdateThrowsSQLException(script);
    }

    @Test
    public void testVariableDeclaredInStatementVisibleFromAnotherStatement() throws SQLException {

        String variableDeclarationScript = "msg <- \"Hello World\" ";
        String printVariableScript = "cat(\"Message::\n\") \n cat(msg) ";

        assertVariableDeclaredInStatementVisibleFromAnotherStatement(variableDeclarationScript, printVariableScript);

    }

    @Test
    public void testHeadersNotSpecified() throws Exception {

        String script = "cat(\"A:\tB:\n\")\n cat(\"First A\tFirst B\n\")\n cat(\"Second A\tSecond B\")";

        assertHeadersNotSpecifiedCausesNumbersToBeUsed(script);
    }

    @Test
    public void testHeadersSpecified() throws Exception {

        String script = "cat(\"A::\tB::\n\")\n cat(\"First A\tFirst B\n\")\n cat(\"Second A\tSecond B\")";

        assertIfHeadersSpecifiedThenHeadersAreUsed(script);
    }

}
