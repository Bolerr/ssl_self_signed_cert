package org.example.ssl.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

import static springfox.documentation.builders.PathSelectors.regex

@Configuration
@ConditionalOnWebApplication
@EnableSwagger2
class SwaggerConfig {

    @Value('${swagger.api-info.title}')
    private String apiTitle

    @Value('${swagger.api-info.desc}')
    private String apiDesc

    @Value('${spring.application.version}')
    private String apiVersion

    @Value('${swagger.api-info.creator}')
    private String apiCreator

    @Value('${swagger.api-info.url-desc}')
    private String apiDocumentationTitle

    @Value('${swagger.api-info.url}')
    private String apiDocumentationUrl

    @Bean
    Docket api() {
        Docket docket =  new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(regex('/api/v.*/.*/.*'))
                .build()
                .apiInfo(new ApiInfo(
                apiTitle,               // Title displayed at top of page
                apiDesc,                // Description of api under title
                apiVersion,             // Version number found at bottom of page
                null,   // Not used
                apiCreator,             // Area responsible for API
                apiDocumentationTitle,  // Hyperlink text to display
                apiDocumentationUrl)    // Hyperlink to further documentation of API
        )

        //Following is used to solve: https://github.com/springfox/springfox/issues/752
        docket.ignoredParameterTypes(groovy.lang.MetaClass.class)

        return docket
    }
}