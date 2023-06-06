// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.config;

import static springfox.documentation.builders.PathSelectors.regex;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
@EnableAutoConfiguration
public class SwaggerConfiguration {

  /**
   * .
   */
  @Bean
  public Docket uprApi() {
    return new Docket(DocumentationType.SWAGGER_2).groupName("user-provisioning-api").apiInfo(apiInfo()).select()
        .paths(uprPaths()).build();
  }

  private ApiInfo apiInfo() {
    return new ApiInfoBuilder().title("User Provisioning Api").description("User Provisioning Api")
        .contact(new Contact("dev", "", "dev.oneplatform@paysafe.com")).version("v1").build();
  }

  private java.util.function.Predicate<String> uprPaths() {
    return regex("/.*" + "v1" + "/.*");
  }
}