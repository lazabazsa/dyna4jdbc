package com.github.dyna4jdbc.internal.processrunner.jdbc.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.github.dyna4jdbc.internal.OutputCapturingScriptExecutor;
import com.github.dyna4jdbc.internal.ScriptExecutionException;
import com.github.dyna4jdbc.internal.config.Configuration;

public final class ProcessRunnerScriptExecutor implements OutputCapturingScriptExecutor {

    private static final int MINIMAL_POLL_INTERVAL_MS = 5;
    private static final int DEFAULT_POLL_INTERVAL_MS = 50;
    private static final int WAIT_BEFORE_CONSUMING_OUTPUT_MS = 1000;

    private final AtomicReference<ProcessRunner> processRunner = new AtomicReference<>();

    private final boolean skipFirstLine;
    private final Configuration configuration;

    public ProcessRunnerScriptExecutor(Configuration configuration) {
        this.configuration = configuration;
        this.skipFirstLine = configuration.getSkipFirstLine();
    }

    @Override
    public void executeScriptUsingStreams(
            String script,
            OutputStream stdOutputStream,
            OutputStream errorOutputStream) throws ScriptExecutionException {

        try (PrintWriter outputPrintWriter = new PrintWriter(new OutputStreamWriter(
                stdOutputStream, configuration.getConversionCharset()), true)) {

            ProcessRunner currentProcess = this.processRunner.get();
            if (currentProcess == null || !currentProcess.isProcessRunning()) {
                currentProcess = ProcessRunner.start(script, configuration.getConversionCharset());
                this.processRunner.set(currentProcess);
            } else {
                currentProcess.writeToStandardInput(script);
            }

            Thread.sleep(WAIT_BEFORE_CONSUMING_OUTPUT_MS);

            if (skipFirstLine) {
                // skip and discard first result line
                String discardedOutput = currentProcess.pollStandardOutput(
                        MINIMAL_POLL_INTERVAL_MS, TimeUnit.SECONDS);
                System.out.println(discardedOutput);
            }

            String outputCaptured = null;

            while (true) {

                if (currentProcess.isErrorEmpty()) {
                    outputCaptured = currentProcess.pollStandardOutput(
                            MINIMAL_POLL_INTERVAL_MS, TimeUnit.SECONDS);
                } else {
                    outputCaptured = currentProcess.pollStandardOutput(
                            DEFAULT_POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);

                    if (outputCaptured == null) {
                        outputCaptured = currentProcess.pollStandardError(
                                DEFAULT_POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
                    }
                }

                if (outputCaptured == null) {
                    break;

                } else {
                    outputPrintWriter.println(outputCaptured);
                }
            }

        } catch (ProcessExecutionException | IOException e) {
            throw new ScriptExecutionException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ScriptExecutionException("Interrupted", e);
        }
    }

    public void close() {

        ProcessRunner currentProcess = this.processRunner.get();
        if (currentProcess != null) {
            currentProcess.terminateProcess();
            this.processRunner.set(null);
        }
    }

}
