package com.tangerine.tangerine.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * [추가] GlobalExceptionHandler
 *
 * @RestControllerAdvice: 모든 @RestController에서 발생하는 예외를
 * 한곳에서 가로채서 처리해요. 이게 있으면 DispatcherServlet이
 * "이 예외, 처리할 핸들러가 있나?"를 찾을 때 여기서 찾아내서
 * 처리하고 끝내요. 그러면 Spring Boot가 /error로 넘길 일이 없어지고,
 * 자연스럽게 Security 필터를 다시 거칠 일도 없어져서
 * 지금까지 겪었던 401 둔갑 현상이 사라져요.
 *
 * 지금까지 Service 계층에서 던졌던 모든
 *   throw new IllegalArgumentException("...")
 * 들이 전부 이 한 곳으로 모여서 처리돼요.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @ExceptionHandler(IllegalArgumentException.class):
     * IllegalArgumentException이 발생하면 이 메서드가 실행돼요.
     * "이메일 중복", "권한 없음", "정의되지 않은 필드" 등
     * 우리가 비즈니스 로직에서 의도적으로 던졌던 예외들이 전부 여기로 와요.
     *
     * 400 Bad Request: "요청 자체가 잘못됐다"는 뜻이에요.
     * 서버 오류(500)가 아니라 클라이언트가 보낸 데이터가
     * 문제라는 걸 명확히 표현해요.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    /**
     * [추가] 그 외 예상하지 못한 모든 예외에 대한 안전망이에요.
     * 이게 없으면 우리가 미리 예상하지 못한 종류의 예외는
     * 여전히 /error로 넘어가서 같은 401 둔갑 현상이 재발해요.
     * Exception.class는 거의 모든 예외의 최상위 부모라서
     * "나머지 전부"를 여기서 받아요.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("서버 내부 오류가 발생했습니다.");
    }
}
