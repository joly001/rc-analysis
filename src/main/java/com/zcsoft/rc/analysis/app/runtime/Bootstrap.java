package com.zcsoft.rc.analysis.app.runtime;

import com.sharingif.cube.context.annotation.ExtendedAnnotationBeanNameGenerator;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Bootstrap
 * 2017/5/21 下午3:32
 *
 * @author Joly
 * @version v1.0
 * @since v1.0
 */
@EnableAutoConfiguration
public class Bootstrap {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .parent(ApplicationContext.class)
                .child(Bootstrap.class)
                .initializers(new ApplicationContextInitializer<GenericApplicationContext>() {
                    @Override
                    public void initialize(GenericApplicationContext applicationContext) {
                        applicationContext.setAllowBeanDefinitionOverriding(false);
                    }
                }).run(args);
    }

    @EnableTransactionManagement(mode=AdviceMode.ASPECTJ)
    @EnableAutoConfiguration
    @ComponentScan(
            basePackages = "com.zcsoft.rc.*.dao,com.zcsoft.rc.analysis.*.dao,com.zcsoft.rc.analysis.*.service,com.zcsoft.rc.analysis.*.scheduled,com.zcsoft.rc.analysis.app.autoconfigure,com.sharingif.cube.spring.boot"
            ,nameGenerator = ExtendedAnnotationBeanNameGenerator.class
            ,useDefaultFilters= false
            ,includeFilters={
                @Filter(type= FilterType.ANNOTATION,value=Repository.class)
                ,@Filter(type=FilterType.ANNOTATION,value=Service.class)
                ,@Filter(type=FilterType.ANNOTATION,value=Component.class)
            })
    protected static class ApplicationContext {
        public static void main(String[] args) {
            new SpringApplicationBuilder(ApplicationContext.class)
                    .initializers(new ApplicationContextInitializer<GenericApplicationContext>() {
                        @Override
                        public void initialize(GenericApplicationContext applicationContext) {
                            applicationContext.setAllowBeanDefinitionOverriding(false);
                        }
                    }).run(args);
        }
    }

}
