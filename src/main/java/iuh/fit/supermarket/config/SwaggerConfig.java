package iuh.fit.supermarket.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Cấu hình Swagger/OpenAPI cho dự án Supermarket
 * Cung cấp documentation tự động cho các REST API
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * Cấu hình OpenAPI documentation
     * 
     * @return OpenAPI configuration
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Development Server"),
                        new Server()
                                .url("https://api.supermarket.com")
                                .description("Production Server")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    /**
     * Thông tin cơ bản về API
     * 
     * @return API Info
     */
    private Info apiInfo() {
        return new Info()
                .title("Supermarket Management System API")
                .description("API documentation cho hệ thống quản lý siêu thị. " +
                        "Hệ thống cung cấp các chức năng quản lý nhân viên, xác thực, phân quyền và các tính năng khác.")
                .version("1.0.0")
                .contact(new Contact()
                        .name("IUH FIT Team")
                        .email("hovinhduy0712@gmail.com")
                        .url("https://fit.iuh.edu.vn"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    /**
     * Cấu hình JWT Bearer token authentication cho Swagger
     * 
     * @return SecurityScheme
     */
    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("Nhập JWT token để xác thực. Token có thể lấy từ endpoint /auth/login");
    }
}
