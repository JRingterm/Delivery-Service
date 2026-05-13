
package com.example.deliver.global.exception;

public record ErrorResponse( //공통 에러 포멧. 아래 형식으로 에러 응답이 출력되도록 한다.
        int status,     //HTTP 상태 코드
        String error,   //HTTP 상태 이름
        String message, //실제 에러 메시지
        String path     //에러가 발생한 요청 URL
) {
}