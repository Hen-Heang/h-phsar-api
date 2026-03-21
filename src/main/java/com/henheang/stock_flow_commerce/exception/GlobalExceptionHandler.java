package com.henheang.stock_flow_commerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NonAuthoritativeInformationException.class)
    ProblemDetail handleNoContentExceptionException(NonAuthoritativeInformationException nonAuthoritativeInformationException){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(203), nonAuthoritativeInformationException.getMessage());
        problemDetail.setType(URI.create("localhost:8080/error/non-authorized"));
        return problemDetail;
    }

    @ExceptionHandler(InternalServerErrorException.class)
    ProblemDetail handleInsertFailedException(InternalServerErrorException internalServerErrorException){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500), internalServerErrorException.getMessage());
        problemDetail.setType(URI.create("localhost:8080/error/internal-server-error"));
        return problemDetail;
    }

    @ExceptionHandler(NoContentException.class)
    ProblemDetail handleAlreadyExistException(NoContentException noContentException){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(204), noContentException.getMessage());
        problemDetail.setType(URI.create("localhost:8080/error/no-content"));
        return problemDetail;
    }

    @ExceptionHandler(OKException.class)
    ProblemDetail handleOKException(OKException okException){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(200), okException.getMessage());
        problemDetail.setType(URI.create("localhost:8080/error/ok"));
        return problemDetail;
    }

    @ExceptionHandler(NotModifiedException.class)
    ProblemDetail handleNotMatchException(NotModifiedException notModifiedException){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(304), notModifiedException.getMessage());
        problemDetail.setType(URI.create("localhost:8080/error/not-modified"));
        return problemDetail;
    }

    @ExceptionHandler(ConflictException.class)
    ProblemDetail handleDuplicateUserException(ConflictException conflictException){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(409), conflictException.getMessage());
        problemDetail.setType(URI.create("localhost:8080/error/conflict"));
        return problemDetail;
    }

    @ExceptionHandler(BadRequestException.class)
    ProblemDetail handleIllegalInputException(BadRequestException badRequestException){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(400), badRequestException.getMessage());
        problemDetail.setType(URI.create("localhost:8080/error/bad-request"));
        return problemDetail;
    }

    @ExceptionHandler(ForbiddenException.class)
    ProblemDetail handleForbiddenException(ForbiddenException forbiddenException){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), forbiddenException.getMessage());
        problemDetail.setType(URI.create("localhost:8080/error/forbidden"));
        return problemDetail;
    }

    @ExceptionHandler(ImATeaPotException.class)
    ProblemDetail handleImATeaPotException(ImATeaPotException imATeaPotException){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(418), imATeaPotException.getMessage());
        problemDetail.setType(URI.create("localhost:8080/error/Im-a-Teapot0_o"));
        return problemDetail;
    }

    @ExceptionHandler(UnauthorizedException.class)
    ProblemDetail handleUnauthorizedException(UnauthorizedException unauthorizedException){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(401), unauthorizedException.getMessage());
        problemDetail.setType(URI.create("localhost:8080/error/unauthorized"));
        return problemDetail;
    }

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail handleNotFoundException(NotFoundException notFoundException){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(404), notFoundException.getMessage());
        problemDetail.setType(URI.create("localhost:8080/error/not-found"));
        return problemDetail;
    }
    @ExceptionHandler(AlreadyExistException.class)
    ProblemDetail handleFoundException(AlreadyExistException alreadyExistException){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(404), alreadyExistException.getMessage());
        problemDetail.setType(URI.create("localhost:8080/error/already-exist"));
        return problemDetail;
    }

    @ExceptionHandler(DefaultValueException.class)
    ProblemDetail handleDefaultValueException(DefaultValueException defaultValueException) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, defaultValueException.getMessage());
        // can create actual endpoint for this URI if required
        problemDetail.setType(URI.create("localhost:8080/error/bad-request"));
        return problemDetail;
    }
}
