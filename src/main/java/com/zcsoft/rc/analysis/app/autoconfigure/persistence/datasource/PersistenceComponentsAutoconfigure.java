package com.zcsoft.rc.analysis.app.autoconfigure.persistence.datasource;

import com.sharingif.cube.persistence.database.DataSourcePoolConfig;
import com.sharingif.cube.security.binary.Base64Coder;
import com.sharingif.cube.security.confidentiality.encrypt.TextEncryptor;
import com.sharingif.cube.security.confidentiality.encrypt.aes.AESECBEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class PersistenceComponentsAutoconfigure {

	@Bean(name="dataSourcePoolConfig")
	public DataSourcePoolConfig createDataSourcePoolConfig(
			@Value("${dataSource.jndi.name}") String jndiName
			,@Value("${dataSource.jndi.type}")String type
			,@Value("${dataSource.driverClassName}")String driverClassName
			,@Value("${dataSource.url}")String url
			,@Value("${dataSource.username}")String username
			,@Value("${dataSource.password}")String password
			,@Value("${dataSource.initialSize}")int initialSize
			,@Value("${dataSource.maxTotal}")int maxTotal
			,@Value("${dataSource.maxWaitMillis}")int maxWaitMillis
			,@Value("${dataSource.maxIdle}")int maxIdle
			,@Value("${dataSource.minIdle}")int minIdle
	) {

		DataSourcePoolConfig dataSourcePoolConfig = new DataSourcePoolConfig();
		dataSourcePoolConfig.setJndiName(jndiName);
		dataSourcePoolConfig.setType(type);
		dataSourcePoolConfig.setDriverClassName(driverClassName);
		dataSourcePoolConfig.setUrl(url);
		dataSourcePoolConfig.setUsername(username);
		dataSourcePoolConfig.setPassword(password);
		dataSourcePoolConfig.setInitialSize(initialSize);
		dataSourcePoolConfig.setMaxTotal(maxTotal);
		dataSourcePoolConfig.setMaxWaitMillis(maxWaitMillis);
		dataSourcePoolConfig.setMaxIdle(maxIdle);
		dataSourcePoolConfig.setMinIdle(minIdle);

		return dataSourcePoolConfig;
	}

	@Bean("dataSourcePoolConfigList")
	public List<DataSourcePoolConfig> createDataSourcePoolConfigList(DataSourcePoolConfig dataSourcePoolConfig) {
		List<DataSourcePoolConfig> dataSourcePoolConfigList = new ArrayList<DataSourcePoolConfig>();
		dataSourcePoolConfigList.add(dataSourcePoolConfig);

		return dataSourcePoolConfigList;
	}

	@Bean("propertyTextEncryptor")
	public TextEncryptor createPropertyTextEncryptor(@Value("${property.key}") String key) throws UnsupportedEncodingException {
		Base64Coder base64Coder = new Base64Coder();
		byte[] keysByte = base64Coder.decode(key);
		AESECBEncryptor encryptor = new AESECBEncryptor(keysByte, base64Coder);

		return encryptor;
	}
	
	@Bean(name="dataSource")
	public JndiObjectFactoryBean jndiDataSource(DataSourcePoolConfig dataSourcePoolConfig) throws IllegalArgumentException, NamingException {
		JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
		jndiObjectFactoryBean.setProxyInterface(DataSource.class);
		jndiObjectFactoryBean.setJndiName(dataSourcePoolConfig.getJndiName());
		jndiObjectFactoryBean.setResourceRef(true);
		return jndiObjectFactoryBean;
	}
	
	@Bean(name="dataSourceTransactionManager")
	public DataSourceTransactionManager createDataSourceTransactionManager(DataSource opayDataSource) {
		DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
		dataSourceTransactionManager.setDataSource(opayDataSource);
		
		return dataSourceTransactionManager;
	}
	
}