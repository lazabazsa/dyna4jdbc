package com.github.dyna4jdbc.integrationtests;

import com.github.dyna4jdbc.internal.JDBCError;
import org.testng.annotations.Test;

import java.sql.*;

import static org.testng.Assert.*;
import static com.github.dyna4jdbc.integrationtests.IntegrationTestUtils.*;

public class ScalaScriptEngineTest extends AbstractScriptEngineIntegrationTest {

    protected ScalaScriptEngineTest() {
        super("jdbc:dyna4jdbc:scriptengine:scala");
    }

    @Test
    public void testWritingFromUpdateThrowsSQLException() {

        String script = "println(\"Hello World\")";

        assertWritingFromUpdateThrowsSQLException(script);
    }

    @Test
    public void testVariableDeclaredInStatementVisibleFromAnotherStatement() throws SQLException {

        String variableDeclarationScript = " var msg : String = \"Hello World\" ";
        String printVariableScript = "println(\"Message::\");\n println(msg) ";

        assertVariableDeclaredInStatementVisibleFromAnotherStatement(variableDeclarationScript, printVariableScript);

    }

    @Test
    public void testHeadersNotSpecified() throws Exception {

        String script = "println(\"A:\tB:\") \n println(\"First A\tFirst B\") \n println(\"Second A\tSecond B\");";

        assertHeadersNotSpecifiedCausesNumbersToBeUsed(script);
    }

    @Test
    public void testHeadersSpecified() throws Exception {

        String script = "println(\"A::\tB::\") \n println(\"First A\tFirst B\") \n print(\"Second A\tSecond B\")";

        assertIfHeadersSpecifiedThenHeadersAreUsed(script);
    }

}
