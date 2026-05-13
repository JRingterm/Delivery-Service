package com.example.deliver.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration //Spring 설정 클래스
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info() //API 기본 정보
                        .title("Deliver API") //API 이름
                        .description("배달 애플리케이션 백엔드 API 문서") //API 설명
                        .version("v1.0.0"))
                .components(new Components() //JWT 인증 설정
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,       //bearerAuth라는 인증 설정 등록.
                                new SecurityScheme() //인증방식 정의
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP) //HTTP 인증 사용
                                        .scheme("bearer")               //Bearer 토큰 방식
                                        .bearerFormat("JWT")))          //토큰 종류
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME)); //이 API들은 bearerAuth 인증을 사용한다.
    }
    //Swagger UI에 Authorize 버튼이 생성된다. 클릭하면 JWT 토큰 입력 가능. -> Swagger에서도 인증 테스트 가능.
}