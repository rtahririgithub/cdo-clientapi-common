
package com.telus.cis.common.core.utils;

import static com.telus.cis.common.core.domain.CommonConstantEnum.CACHE_CONTROL;
import static com.telus.cis.common.core.domain.CommonConstantEnum.COMMA_DELIMITER;
import static com.telus.cis.common.core.domain.CommonConstantEnum.ENV_STRING;
import static com.telus.cis.common.core.domain.CommonConstantEnum.ORIGINATING_CLIENT;
import static com.telus.cis.common.core.domain.CommonConstantEnum.USER_ID_TOKEN_HEADER;
import static com.telus.cis.common.core.domain.CommonConstantEnum.VARIABLE_PATTERN;
import static com.telus.cis.common.core.domain.CommonConstantEnum.X_RESULT_COUNT;
import static com.telus.cis.common.core.domain.CommonConstantEnum.X_RUNTIME_ENV;
import static com.telus.cis.common.core.domain.CommonConstantEnum.X_TOTAL_COUNT;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.telus.cis.common.core.domain.CommonConstantEnum;
import com.telus.cis.common.core.domain.ServiceStatusCode;
import com.telus.cis.common.core.exception.ValidationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiUtils {

	public static final String REFERENCE_HOST = "x-forwarded-host";
	public static final String REFERENCE_PORT = "x-forwarded-port";
	public static final String REFERENCE_PROTO = "x-forwarded-proto";
	public static final String END_USER_HEADER = "x-telus-enduserid";
	public static final String SOA_APPLICATION_HEADER = "x-telus-soa-appid";
	public static final String SDF_APPLICATION_HEADER = "x-telus-sdf-appid";
	public static final String ORGINATING_APP_HEADER = "x-telus-originating-appid";
	public static final String HTTP_SERVLET_REQUEST_ATTR_AUTHORIZED_CLIENT = "authorized_client";
	private static final String CLIENT_ID = "client_id";

	private ApiUtils() {
		// private constructor to override Java's implicit public constructor
		throw new IllegalStateException("Utility classes should not have public constructors.");
	}

	public static <T> String prettyWrite(ObjectMapper objectMapper, T value) {

		try {
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
		} catch (JsonProcessingException e) {
			log.warn("json pretty write error {}", e.getLocalizedMessage());
			log.trace("JsonProcessingException exception ", e);
			return value.toString();
		}
	}

	/**
	 * processElements(personList, p -> p.getAge() > 10, p -> p.toString(), person -> System.out.println(person));
	 */
	public static <X, Y> void processElements(Iterable<X> source, Predicate<X> tester, Function<X, Y> mapper, Consumer<Y> consumer) {

		for (X p : source) {
			if (tester.test(p)) {
				Y data = mapper.apply(p);
				consumer.accept(data);
			}
		}
	}

	public static List<Field> getAllFields(Class<?> classType) {
		return getAllFields(new ArrayList<>(), classType);
	}

	public static List<Field> getAllFields(List<Field> fields, Class<?> classType) {

		Arrays.asList(classType.getDeclaredFields()).forEach(field -> {
			if (!Modifier.isStatic(field.getModifiers())) {
				fields.add(field);
			}
		});
		if ((classType.getSuperclass() != null)) {
			getAllFields(fields, classType.getSuperclass());
		}

		return fields;
	}

	public static <T> T cloneEntity(T entity) {
		return cloneEntity(null, entity);
	}

	/**
	 * If newEntity is null, create new instance Perform shallow copy by PropertyUtils.copyProperties()
	 */
	@SuppressWarnings("unchecked")
	public static <T> T cloneEntity(T newEntity, T entity) {

		try {
			if (newEntity == null) {
				newEntity = (T) entity.getClass().getDeclaredConstructor().newInstance();
			}
			PropertyUtils.copyProperties(newEntity, entity);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
			log.warn("cloneEntity {} {}", e.getClass().getSimpleName(), e.getLocalizedMessage());
			log.trace("new instance exception", e);
		}

		return newEntity;
	}

	public static <T> List<T> cloneEntityList(List<T> originList) {

		List<T> cloneList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(originList)) {
			originList.stream().filter(Objects::nonNull).forEach(t -> cloneList.add(cloneEntity(t)));
		}

		return cloneList;
	}

	public static <T> void filterEntity(T entity, Set<String> filters) {
		filterEntity(entity, filters, null);
	}

	public static <T> void filterEntity(T entity, Set<String> filters, Set<String> defaults) {

		Set<String> allFilters = new HashSet<>();
		if (CollectionUtils.isNotEmpty(filters)) {
			allFilters.addAll(filters);
		}
		if (CollectionUtils.isNotEmpty(defaults)) {
			allFilters.addAll(defaults);
		}
		if (CollectionUtils.isNotEmpty(allFilters)) {
			List<Field> fields = getAllFields(entity.getClass());
			fields.stream().filter(field -> !allFilters.contains(field.getName())).forEach(field -> {
				try {
					PropertyUtils.setProperty(entity, field.getName(), null);
				} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
					log.warn("filterEntity {} {}", e.getClass().getSimpleName(), e.getLocalizedMessage());
					log.trace("setProperty exception", e);
				}
			});
		}
	}

	public static Map<String, String> initResponsesHeaders(String... activeProfiles) {
		return isNull(activeProfiles) 
				? new HashMap<>()
				: Stream.of(Pair.of(X_RUNTIME_ENV.value(), String.join(",", activeProfiles))).filter(p -> StringUtils.isNotBlank(p.getRight()))
						.collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
	}
	
	
	public static <T> boolean validateObject(T object)
    {
	    return validateObject( ServiceStatusCode.ERROR_VALIDATE.getCode(), object, null);
    }
	
	
	public static <T> boolean validateObject(T object, Errors errors)
	{
	    return validateObject( ServiceStatusCode.ERROR_VALIDATE.getCode(), object, errors);
	}
	
	
	public static <T> boolean validateObject(String code, T object, Errors errors)
    {
        Set<ConstraintViolation<T>> violations = Validation
                .buildDefaultValidatorFactory().getValidator()
                .validate( object );
        StringBuilder errorString = new StringBuilder();
        violations.forEach( violation -> {
            log.debug( "validation error {} {} ", violation.getPropertyPath(),
                    violation.getMessage() );
            if ( errorString.length() > 0 ) {
                errorString.append( "\n" );
            }
            errorString.append( violation.getPropertyPath() + " : "
                    + violation.getMessage() );
        } );
        if ( errorString.length() > 0 ) {
            if ( isNull( errors ) ) {
                throw new ValidationException( code, errorString.toString() );
            } 
            return false;
        }
        return true;
    }
	

	public static void validateParamsMap(Map<String, String> headers, Map<String, String> allParams, String... activeProfiles) {
		headers.putAll(initResponsesHeaders(activeProfiles));
		validateParamsMap(allParams);
	}

	public static void validateParamsMap(Map<String, String> allParams) {

		if (!isNull(allParams)) {
			Map<String, String> tempMap = new HashMap<>();

			for (Entry<String, String> entry : allParams.entrySet()) {
				if (StringUtils.containsWhitespace(entry.getKey())) {
					tempMap.put(entry.getKey(), entry.getKey().trim());
				}
			}

			for (Entry<String, String> tempEntry : tempMap.entrySet()) {
				allParams.put(tempEntry.getValue(), allParams.remove(tempEntry.getKey()));
			}
		}
	}

	public static HttpHeaders injectHeaders(HttpHeaders headers, Map<String, String> headerParams, Map<String, String> params) {

		if (MapUtils.isNotEmpty(headerParams)) {
			Stream.of(ENV_STRING.value(), X_RUNTIME_ENV.value(), CACHE_CONTROL.value(), ORIGINATING_CLIENT.value(), USER_ID_TOKEN_HEADER.value())
					.filter(key -> StringUtils.isNotBlank(headerParams.get(key))).forEach(key -> headers.add(key, headerParams.get(key)));
		}

		if (MapUtils.isNotEmpty(params)) {
			params.forEach(headers::add);
		}

		return headers;
	}

	@SafeVarargs
	public static <K, V> Map<K, V> filterMap(Map<K, V> map, K... keyValues) {

		Map<K, V> newMap = new HashMap<>();
		if (MapUtils.isNotEmpty(map)) {
			Stream.of(keyValues).filter(key -> nonNull(map.get(key))).forEach(key -> newMap.put(key, map.get(key)));
		}

		return newMap;
	}

	/**
	 * Builds a response entity for a single object with the given HTTP status
	 */
	public static <T> ResponseEntity<T> buildResponseEntity(T body, HttpStatus status, Map<String, String> headerParams, Map<String, String> params) {

		// build the response headers
		params = (params == null || params.isEmpty()) ? new HashMap<>(4) : params;
		params.put(X_TOTAL_COUNT.value(), String.valueOf(1));
		params.put(X_RESULT_COUNT.value(), String.valueOf(1));
		HttpHeaders headers = injectHeaders(new HttpHeaders(), headerParams, params);

		// build the response entity
		BodyBuilder responseBuilder = ResponseEntity.status(status);

		return responseBuilder.headers(headers).body(body);
	}

	/**
	 * Builds a 200 OK response entity for a single object
	 */
	public static <T> ResponseEntity<T> buildResponseEntity(T body, Map<String, String> headerParams, Map<String, String> params) {
		return buildResponseEntity(body, HttpStatus.OK, headerParams, params);
	}

	public static <T> ResponseEntity<T> buildResponseEntity(T body, Map<String, String> headerParams) {
		return buildResponseEntity(body, HttpStatus.OK, headerParams, null);
	}

	/**
	 * Builds a response entity for retrieving a list of objects with support for 206 PARTIAL_CONTENT
	 */
	public static <T> ResponseEntity<List<T>> buildResponseEntity(List<T> body, Long totalCount, Map<String, String> headerParams, Map<String, String> params) {

		// build the response headers
		params = (params == null || params.isEmpty()) ? new HashMap<>(4) : params;
		params.put(X_TOTAL_COUNT.value(), String.valueOf(totalCount));
		params.put(X_RESULT_COUNT.value(), String.valueOf(body.size()));
		HttpHeaders headers = injectHeaders(new HttpHeaders(), headerParams, params);

		// build the response entity
		boolean paging = !isNull(totalCount) && totalCount > body.size();
		BodyBuilder responseBuilder = paging ? ResponseEntity.status(HttpStatus.PARTIAL_CONTENT) : ResponseEntity.ok();

		return responseBuilder.headers(headers).body(body);
	}

	public static <T> ResponseEntity<List<T>> buildResponseEntity(List<T> body, Long totalCount, Map<String, String> headerParams) {
		return buildResponseEntity(body, totalCount, headerParams, null);
	}

	/**
	 * Builds a request URI string for a given list of parameters and path 
	 * @deprecated in favour of buildRequestUriString 
	 */
	@Deprecated
	public static String buildRequestParamUri(Map<String, String> params, String path) {
		return buildRequestUriString(params, path);
	}

	/**
	 * Builds a request URI for a given list of query parameters, URI path and URI variables
	 */
	public static URI buildRequestUri(Map<String, String> queryParams, String uriString, Object... uriVariables) {
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uriString);
		Optional.ofNullable(queryParams).ifPresent(map -> map.entrySet().stream().forEach(entry -> builder.queryParam(entry.getKey(), entry.getValue())));
		
		return builder.build(uriVariables);
	}

	/**
	 * Builds a request URI string for a given list of query parameters, URI path and URI variables
	 */
	public static String buildRequestUriString(Map<String, String> queryParams, String uriString, Object... uriVariables) {
		return buildRequestUri(queryParams, uriString, uriVariables).toString();
	}

	public static Set<String> convertStringToSet(String str) {
		return convertStringToSet(str, COMMA_DELIMITER.value());
	}

	public static Set<String> convertStringToSet(String str, String delimiter) {

		if (StringUtils.isNotBlank(str)) {
			return Arrays.asList(StringUtils.strip(str).split(delimiter)).stream().map(StringUtils::strip).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
		}

		return Collections.emptySet();
	}

	public static Set<String> decodeStringToSet(String str, String delimiter) {
		return convertStringToSet(decodeString(str), delimiter);
	}

	public static String decodeString(String str) {

		if (StringUtils.isNotBlank(str)) {
			try {
				return URLDecoder.decode(str, StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				log.warn("decodeStringToSet {} {}", e.getClass().getSimpleName(), e.getLocalizedMessage());
				log.trace("decoder exception", e);
			}
		}

		return StringUtils.EMPTY;
	}

	/**
	 * Extract element
	 */
	public static Boolean extractBooleanValue(Map<String, String> allParams, String label, Boolean defaultValue) {

		if (StringUtils.isEmpty(label)) {
			return defaultValue;
		}
		String labelValue = allParams.get(label);
		if ("null".equals(labelValue)) {
			labelValue = null;
		}

		return (labelValue == null) ? defaultValue : Boolean.valueOf(labelValue);
	}

	public static Long extractLongValue(Map<String, String> allParams, String label, Long defaultValue) {

		if (StringUtils.isEmpty(label)) {
			return defaultValue;
		}
		String labelValue = allParams.get(label);
		try {
			return (labelValue == null) ? defaultValue : Long.valueOf(labelValue.trim());
		} catch (NumberFormatException ex) {
			log.warn("convert exception {} : {}", labelValue, ex.getLocalizedMessage());
		}

		return defaultValue;
	}

	public static <T extends Enum<T>> T locateEnum(Class<T> enumType, String enumName, T defaultEnum) {
		return Arrays.stream(enumType.getEnumConstants()).filter(t -> t.name().equals(enumName)).findFirst().orElse(defaultEnum);
	}

	public static <V> Map<String, V> convertToCaseInsensitiveMap(Map<String, V> map) {

		Map<String, V> caseInsensitiveMap = new LinkedCaseInsensitiveMap<>();
		caseInsensitiveMap.putAll(map);
		log.debug("Case-insensitive map values {}", caseInsensitiveMap);

		return caseInsensitiveMap;
	}

	public static byte getFirstByteFromString(final String str) {
		return StringUtils.defaultIfBlank(str, StringUtils.SPACE).getBytes()[0];
	}

	public static <T> void setIfNonNull(final Consumer<T> setter, T value) {
		setIfNonNull(setter, null, value);
	}

	/**
	 * usage: ApiUtils.setIfNonNull( entity::setId, String::toUpperCase, "abcd" ); -> entity value set to ABCD
	 */
	public static <T> void setIfNonNull(final Consumer<T> setter, final UnaryOperator<T> modifier, T value) {
		
		if (nonNull(value)) {
			setter.accept(isNull(modifier) ? value : modifier.apply(value));
		}
	}

	/**
	 * usage: ApiUtils.setIfNonNull( entity::setId, Integer::parseInt, "12345" ); -> entity value set to integer value of "12345"
	 */
	public static <T, U> void setIfNonNull(final Consumer<T> setter, final Function<U, T> translator, U value) {
		
		if (nonNull(value)) {
			setter.accept(translator.apply(value));
		}
	}

	/**
	 * usage: set value with setter consumer with value if getter value is null.
	 */
	public static <T> void setIfNull(final Supplier<T> getter, final Consumer<T> setter, T value) {
		
		T getterValue = getter.get();
		if (isNull(getterValue)) {
			setter.accept(value);
		}
	}

	/**
	 * <ol>
	 * <li>get the value by getter
	 * <li>if getter value is null, use the providedValue
	 * <li>if the providedValue is use, activate setter to use the provided value
	 * </ol>
	 */
	public static void setIfNullWithSecretValidation(Supplier<String> getter, Consumer<String> setter, String providedValue) {
		
		String value = validateSecretVariable(Optional.ofNullable(getter.get()).orElse(providedValue));
		if (StringUtils.equals(value, providedValue)) {
			setter.accept(providedValue);
		}
	}

	public static String validateSecretVariable(String variable) {
		
		if (StringUtils.isNotBlank(variable) && variable.matches(VARIABLE_PATTERN.value())) {
			throw new ValidationException("variable " + variable + " is not translated");
		}
		
		return variable;
	}

	public static String jwtDecodeClientId(String jwtToken) {
		
		if (isNull(jwtToken)) {
			return null;
		}
		String[] splitToken = jwtToken.split("\\.");
		
		return (splitToken.length < 2) ? null : new String(Base64.getUrlDecoder().decode(splitToken[1])).replaceFirst("^.*\"" + CLIENT_ID + "\":\"(.*?)\",.*$", "$1");
	}

	public static Map<String, String> defaultOriginatingClient(Map<String, String> headers, String defaultClientId) {
		// preserve the originating client header value, if there is one - otherwise default the clientId
		headers.put(CommonConstantEnum.ORIGINATING_CLIENT.value(), StringUtils.defaultIfBlank(headers.get(CommonConstantEnum.ORIGINATING_CLIENT.value()), defaultClientId));
		return headers;
	}

	/**
	 * Add (String) parameter values to a map given a parameter name and sequence of values. Existing values in the map are preserved or a new map is created if the
	 * supplied map is null. The format of the added/modified parameter value is 'parameterName=value1,value2,...' and the value is placed back in the map with the
	 * parameter name as the key.
	 */
	public static Map<String, String> addParameterValues(Map<String, String> map, String parameter, String... values) {

		Map<String, String> params = Optional.ofNullable(map).orElseGet(HashMap::new);
		Set<String> parameterSet = ApiUtils.decodeStringToSet(params.get(parameter), CommonConstantEnum.COMMA_DELIMITER.value()).stream().collect(Collectors.toSet());
		if (ArrayUtils.isNotEmpty(values)) {
			Arrays.asList(values).forEach(parameterSet::add);
		}
		params.put(parameter, String.join(CommonConstantEnum.COMMA_DELIMITER.value(), parameterSet));

		return params;
	}

}