package com.github.dyna4jdbc.internal.config;

import java.sql.SQLException;
import java.util.Properties;

public interface ConfigurationFactory {

	Configuration newConfigurationFromParameters(String config, Properties props) throws SQLException;
}
