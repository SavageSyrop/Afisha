package ru.it.lab.configuration;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spring.web.DescriptionResolver;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

import java.util.Optional;
import java.util.logging.Level;

@Component
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER + 1)
@Slf4j
public class OperationNotesResourcesReader implements OperationBuilderPlugin {
    private final DescriptionResolver descriptions;

    @Autowired
    public OperationNotesResourcesReader(DescriptionResolver descriptions) {
        this.descriptions = descriptions;
    }

    @Override
    public void apply(OperationContext context) {
        try {
            StringBuilder sb = new StringBuilder();

            // Check authorization
            Optional<PreAuthorize> preAuthorizeAnnotation = context.findAnnotation(PreAuthorize.class);
            sb.append("<b>Access Privileges & Level</b>: ");
            if (preAuthorizeAnnotation.isPresent()) {
                sb.append("<em>" + preAuthorizeAnnotation.get().value() + "</em>");
            } else {
                sb.append("<em>FREE ACCESS (UNAUTHORIZED)</em>");
            }

            // Check notes
            Optional<ApiOperation> annotation = context.findAnnotation(ApiOperation.class);
            if (annotation.isPresent() && StringUtils.hasText(annotation.get().notes())) {
                sb.append("<br /><br />");
                sb.append(annotation.get().notes());
            }

            // Add the note text to the Swagger UI
            context.operationBuilder().notes(descriptions.resolve(sb.toString()));
        } catch (Exception e) {
            log.error( "Error when creating swagger documentation for security roles: ", e);
        }
    }

    @Override
    public boolean supports(DocumentationType documentationType) {
        return SwaggerPluginSupport.pluginDoesApply(documentationType);
    }
}
