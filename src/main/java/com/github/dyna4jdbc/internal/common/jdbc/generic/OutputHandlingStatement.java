package com.github.dyna4jdbc.internal.common.jdbc.generic;

import java.io.PrintWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.github.dyna4jdbc.internal.OutputCapturingScriptExecutor;
import com.github.dyna4jdbc.internal.OutputDisabledError;
import com.github.dyna4jdbc.internal.RuntimeDyna4JdbcException;
import com.github.dyna4jdbc.internal.JDBCError;
import com.github.dyna4jdbc.internal.ScriptExecutionException;
import com.github.dyna4jdbc.internal.common.jdbc.base.AbstractStatement;
import com.github.dyna4jdbc.internal.common.outputhandler.MultiTypeScriptOutputHandler;
import com.github.dyna4jdbc.internal.common.outputhandler.ScriptOutputHandler;
import com.github.dyna4jdbc.internal.common.outputhandler.ScriptOutputHandlerFactory;
import com.github.dyna4jdbc.internal.common.outputhandler.SingleResultSetScriptOutputHandler;
import com.github.dyna4jdbc.internal.common.outputhandler.UpdateScriptOutputHandler;
import com.github.dyna4jdbc.internal.common.util.exception.ExceptionUtil;

public class OutputHandlingStatement<T extends java.sql.Connection> extends AbstractStatement<T> {

    private final ScriptOutputHandlerFactory scriptOutputHandlerFactory;
	private final OutputCapturingScriptExecutor outputCapturingScriptExecutor;

	public OutputHandlingStatement(
			T connection, 
			ScriptOutputHandlerFactory scriptOutputHandlerFactory, 
			OutputCapturingScriptExecutor outputCapturingScriptExecutor) {
		
        super(connection);
        this.scriptOutputHandlerFactory = scriptOutputHandlerFactory;
        this.outputCapturingScriptExecutor = outputCapturingScriptExecutor;
    }

    public final ResultSet executeQuery(String script) throws SQLException {
    	checkNotClosed();
    	
    	try {
            SingleResultSetScriptOutputHandler outputHandler =
			        scriptOutputHandlerFactory.newSingleResultSetScriptOutputHandler(this, script);
			
			executeScriptUsingOutputHandler(script, outputHandler);
			
			return outputHandler.getResultSet();
			
        } 
        catch (ScriptExecutionException se) {
        	String message = ExceptionUtil.getRootCauseMessage(se);
            throw JDBCError.SCRIPT_EXECUTION_EXCEPTION.raiseSQLException(se, message);
        } 
        catch (SQLException sqle) {
            throw sqle;
        } 
        catch (RuntimeDyna4JdbcException ex) {
            throw new SQLException(ex.getMessage(), ex);
        }
        catch (Throwable t) {
        	String message = ExceptionUtil.getRootCauseMessage(t);
            throw JDBCError.UNEXPECTED_THROWABLE.raiseSQLException(t, message);
        }
    }
    
    public final int executeUpdate(final String script) throws SQLException {
    	checkNotClosed();
    	
        try {
            UpdateScriptOutputHandler outputHandler =
			        scriptOutputHandlerFactory.newUpdateScriptOutputHandler(this, script);
			
			executeScriptUsingOutputHandler(script, outputHandler);
			
			return outputHandler.getUpdateCount();

        } 
        catch (ScriptExecutionException se) {
        	String message = ExceptionUtil.getRootCauseMessage(se);
            throw JDBCError.SCRIPT_EXECUTION_EXCEPTION.raiseSQLException(se, message);
        } 
        catch (RuntimeDyna4JdbcException ex) {
            throw new SQLException(ex.getMessage(), ex);
        }
        catch (OutputDisabledError t) {
        	String message = ExceptionUtil.getRootCauseMessage(t);
        	throw JDBCError.USING_STDOUT_FROM_UPDATE.raiseSQLException(t, message);
        }
        catch (Throwable t) {
        	String message = ExceptionUtil.getRootCauseMessage(t);
            throw JDBCError.UNEXPECTED_THROWABLE.raiseSQLException(t, message);
        }
    }

    public final boolean execute(final String script) throws SQLException {
    	checkNotClosed();
    	
        try {
        	checkNotClosed();
        	
			MultiTypeScriptOutputHandler outputHandler =
			        scriptOutputHandlerFactory.newMultiTypeScriptOutputHandler(this, script);
			
			executeScriptUsingOutputHandler(script, outputHandler);
			
			boolean resultSets = outputHandler.isResultSets();
			if(resultSets) {
			    List<ResultSet> resultSetList = outputHandler.getResultSets();
			    setCurrentResultSetList(resultSetList);
			    setUpdateCount(-1);
			    
			} else {
			    int updateCount = outputHandler.getUpdateCount();
			    setUpdateCount(updateCount);
			}
			
			return resultSets;
        }
        catch (ScriptExecutionException se) {
        	String message = ExceptionUtil.getRootCauseMessage(se);
            throw JDBCError.SCRIPT_EXECUTION_EXCEPTION.raiseSQLException(se, message);
        }
        catch (RuntimeDyna4JdbcException ex) {
            throw new SQLException(ex.getMessage(), ex);
        }
        catch (Throwable t) {
        	String message = ExceptionUtil.getRootCauseMessage(t);
            throw JDBCError.UNEXPECTED_THROWABLE.raiseSQLException(t, message);
        }
    }

    
    private void executeScriptUsingOutputHandler(
            String script, ScriptOutputHandler scriptOutputHandler) throws ScriptExecutionException {

        PrintWriter outPrintWriter = scriptOutputHandler.getOutPrintWriter();
        PrintWriter errorPrintWriter = scriptOutputHandler.getErrorPrintWriter();

        executeScriptUsingCustomWriters(script, outPrintWriter, errorPrintWriter);

        if(outPrintWriter != null) {
            outPrintWriter.flush();
            outPrintWriter.close();
        }
        
        if(errorPrintWriter != null) {
        	errorPrintWriter.flush();
        	errorPrintWriter.close();
        }
    }

    private void executeScriptUsingCustomWriters(final String script, Writer outWriter, Writer errorWriter)
            throws ScriptExecutionException {

    	
    	outputCapturingScriptExecutor.executeScriptUsingCustomWriters(script, outWriter, errorWriter);
    	
    }
}
