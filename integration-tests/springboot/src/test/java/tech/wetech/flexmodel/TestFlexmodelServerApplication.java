package tech.wetech.flexmodel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestFlexmodelServerApplication {

	public static void main(String[] args) {
		SpringApplication.from(FlexmodelServerApplication::main).with(TestFlexmodelServerApplication.class).run(args);
	}

}
