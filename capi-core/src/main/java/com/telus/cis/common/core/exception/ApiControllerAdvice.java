package com.telus.cis.common.core.exception;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiControllerAdvice {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Errors> resourceNotFoundException(final ResourceNotFoundException e) {
		log.error("Handle for ResourceNotFoundException:", e);
		return error(e, getStatus(e.getApiError()), e.getApiError().getReason(), e.getApiError().getCode());
	}

	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<Errors> validationException(final ValidationException e) {
		log.error("Handle for ValidationException:", e);
		return error(e, getStatus(e.getApiError()), e.getApiError().getReason(), e.getApiError().getCode());
	}

	@ExceptionHandler(MethodNotAllowedException.class)
	public ResponseEntity<Errors> methodNotAllowedException(final MethodNotAllowedException e) {
		log.error("Handle for MethodNotAllowedException:", e);
		return error(e, getStatus(e.getApiError()), e.getApiError().getReason(), e.getApiError().getCode());
	}

	@ExceptionHandler(DownstreamServiceException.class)
	public ResponseEntity<Errors> downstreamServiceException(final DownstreamServiceException e) {
		log.error("Handle for DownstreamServiceException:", e);
		return error(e, getStatus(e.getApiError()), e.getApiError().getReason(), e.getApiError().getCode());
	}

	@ExceptionHandler(ForbiddenException.class)
	public ResponseEntity<Errors> forbiddenException(final ForbiddenException e) {
		log.error("Handle for ForbiddenException:", e);
		return error(e, getStatus(e.getApiError()), e.getApiError().getReason(), e.getApiError().getCode());
	}

	@ExceptionHandler(IdentityValidationException.class)
	public ResponseEntity<Errors> identityValidationException(final IdentityValidationException e) {
		log.error("Handle for IdentityValidationException:", e);
		return error(e, getStatus(e.getApiError()), e.getApiError().getReason(), e.getApiError().getCode());
	}

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<Errors> apiException(final ApiException e) {
		log.error("Handle for ApiException:", e);
		return error(e, getStatus(e.getApiError()), e.getApiError().getReason(), e.getApiError().getCode(),
				e.getApiError().getMessage());
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<Errors> unknownException(RuntimeException e) {
		log.error("Handle for RuntimeException:", e);
		return error(e, HttpStatus.INTERNAL_SERVER_ERROR, "UNKNOWN", "UNKNOWN_ERROR");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Errors> exception(Exception e) {
		log.error("Handle for Exception:", e);
		return error(e, HttpStatus.INTERNAL_SERVER_ERROR, "UNKNOWN", "UNKNOWN_ERROR");
	}

	@ExceptionHandler(Throwable.class)
	public ResponseEntity<Errors> throwable(final Throwable t) {
		log.error("Handle for Throwable:", t);
		return new ResponseEntity<>(new Errors("General error", t.getMessage(), "ERROR_GENERIC",
				HttpStatus.INTERNAL_SERVER_ERROR.toString()), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Errors> methodArgumentNotValidException(final MethodArgumentNotValidException e) {
		log.error("Handle for MethodArgumentNotValidException:", e);
		return error(e, HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST.toString());
	}


	@ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Errors> responseStatusException(final ResponseStatusException e) {
        log.error("Handle for ResponseStatusException:", e);
        Exception exception = Optional.ofNullable( e.getCause() ).filter( Exception.class::isInstance ).map( Exception.class::cast ).orElse( e );
        return error( exception, e.getStatus(), e.getStatus().getReasonPhrase(), e.getStatus().toString(), e.getReason() );
    }


    @ExceptionHandler( { UnsupportedOperationException.class } )
    public ResponseEntity<Errors>  unsupportOperationException(final Exception e) {
        log.error("Handle for {}:", e.getClass().getSimpleName());
        return error(e, HttpStatus.NOT_IMPLEMENTED, HttpStatus.NOT_IMPLEMENTED.getReasonPhrase(), HttpStatus.NOT_IMPLEMENTED.toString() );
    }




	protected <E extends Exception> ResponseEntity<Errors> error(final E exception, final HttpStatus httpStatus,
			final String reason, final String errorCode) {
		return error(exception, httpStatus, reason, errorCode, null);
	}

	protected <E extends Exception> ResponseEntity<Errors> error(final E exception, final HttpStatus httpStatus,
			final String reason, final String errorCode, final String message) {
		String exceptionMessage = Optional.ofNullable(message)
				.filter(StringUtils::isNotBlank)
				.orElseGet(() -> Optional.ofNullable(exception.getMessage())
						.filter(StringUtils::isNotBlank)
						.orElseGet(() -> exception.getClass().getSimpleName()));
		return new ResponseEntity<>(new Errors(reason, exceptionMessage, errorCode, String.valueOf(httpStatus.value())),
				httpStatus);
	}

	protected HttpStatus getStatus(ApiError apiError) {
		return Optional.ofNullable(apiError.getStatus()).isPresent() ? HttpStatus.valueOf(apiError.getStatus())
				: HttpStatus.INTERNAL_SERVER_ERROR;
	}

	protected static class Errors {

		@JsonProperty("code")
		String code;

		@JsonProperty("reason")
		String reason;

		@JsonProperty("message")
		String message;

		@JsonProperty("status")
		String status;

		public Errors(String reason, String message, String code, String status) {
			this.code = code;
			this.reason = reason;
			this.message = message;
			this.status = status;
		}
	}

}