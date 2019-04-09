package com.zcsoft.rc.analysis.app.autoconfigure.persistence.mybatis;

import com.sharingif.cube.persistence.mybatis.interceptor.PaginationInterceptor;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
public class MyBatisConfigurationAutoconfigure {


	
	@Bean(name="sqlSessionFactory")
	public SqlSessionFactoryBean getSqlSessionFactory(DataSource dataSource, PaginationInterceptor paginationInterceptor) throws IOException {
		SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
		sqlSessionFactory.setDataSource(dataSource);
		sqlSessionFactory.setPlugins(new Interceptor[]{paginationInterceptor});
		sqlSessionFactory.setConfigLocation(new ClassPathResource("config/mybatis/MyBatisConfiguration.xml"));
		Resource[] resources = new PathMatchingResourcePatternResolver().getResources("classpath*:config/mybatis/mappers/*.xml");
		sqlSessionFactory.setMapperLocations(resources);
		
		return sqlSessionFactory;
	}
	
	@Bean(name="sqlSessionTemplate")
	public SqlSessionTemplate getSqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}
	
}
