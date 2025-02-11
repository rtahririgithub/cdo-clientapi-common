/*
 *  Copyright (c) 2020 TELUS Communications Inc.,
 *  All Rights Reserved.
 *
 *  This document contains proprietary information that shall be
 *  distributed or routed only within TELUS, and its authorized
 *  clients, except with written permission of TELUS.
 *
 * $Id$
 */

package com.telus.cis.common.core.utils;

import static com.telus.cis.common.core.domain.CommonConstantEnum.ENV_STRING;
import static com.telus.cis.common.core.domain.CommonConstantEnum.X_RUNTIME_ENV;
import static com.telus.cis.common.core.utils.ApiUtils.extractLongValue;
import static com.telus.cis.common.core.utils.ApiUtils.prettyWrite;
import static com.telus.cis.common.core.utils.ApiUtils.validateParamsMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telus.cis.common.core.domain.CommonConstantEnum;
import com.telus.cis.common.core.exception.ValidationException;
import com.telus.cis.common.core.module.EntityModule;
import com.telus.cis.common.core.module.TestEnum;
import com.telus.cis.common.core.test.JunitTestUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class ApiUtilsTest {
	
	private ObjectMapper objectMapper;

	@BeforeEach
	public void setUp() throws Exception {
		objectMapper = JunitTestUtils.getObjectMapper();
	}

	@Test
	void testAllFields() {
		
		String serialUid = "serialVersionUID";

		List<Field> fields = ApiUtils.getAllFields(EntityModule.class);
		log.info("{}", ApiUtils.prettyWrite(objectMapper, fields.stream().map(Field::getName).collect(Collectors.toList())));

		assertTrue(fields.stream().noneMatch(field -> serialUid.equalsIgnoreCase(field.getName())));
	}

	@Test
	void testFilterEntity() {
		
		Set<String> filters = new HashSet<>(Arrays.asList("Id", "bigDecimal"));
		EntityModule entity = new EntityModule();
		entity.setBigDecimal(BigDecimal.valueOf(1.0D));
		entity.setDateVal(OffsetDateTime.now());
		entity.setId("1234");
		entity.setNumberVal1(9876);
		entity.setNumberVal2(8765);
		entity.setStringVal("somevalue");

		log.trace("entity module {}", ApiUtils.prettyWrite(objectMapper, entity));
		assertNotNull(entity.getDateVal());
		ApiUtils.filterEntity(entity, filters);

		log.info("entity module {}", ApiUtils.prettyWrite(objectMapper, entity));
		assertNull(entity.getDateVal());
	}

	@Test
	void testFilterEntity_withDefaults() {
		
		Set<String> filters = new HashSet<>(Arrays.asList("Id", "bigDecimal"));
		Set<String> defaults = new HashSet<>(Arrays.asList("numberVal1"));
		EntityModule entity = new EntityModule();
		entity.setBigDecimal(BigDecimal.valueOf(1.0D));
		entity.setDateVal(OffsetDateTime.now());
		entity.setId("1234");
		entity.setNumberVal1(9876);
		entity.setNumberVal2(8765);
		entity.setStringVal("somevalue");

		ApiUtils.filterEntity(entity, filters, defaults);
		log.info("entity module {}", ApiUtils.prettyWrite(objectMapper, entity));
		assertThat(entity.getNumberVal1(), equalTo(9876));
	}

	@Test
	void testFilterEntity2() {
		
		Set<String> filters = new HashSet<>(Arrays.asList("stringVal", "numberVal3"));

		EntityModule entity = new EntityModule();

		entity.setStringVal("somevalue");

		ApiUtils.filterEntity(entity, filters);
		log.info("entity module {}", ApiUtils.prettyWrite(objectMapper, entity));
	}

	@Test
	void testConvertString() {
		
		Set<String> stringSet = ApiUtils.convertStringToSet(" a , b , c ", ",");
		log.info("set {}", prettyWrite(objectMapper, stringSet));
		assertThat(stringSet.size(), equalTo(3));

		stringSet = ApiUtils.convertStringToSet("d", ",");
		log.info("set {}", prettyWrite(objectMapper, stringSet));
	}

	@Test
	void testConvertString_withNullString() {
		
		Set<String> stringSet = ApiUtils.convertStringToSet("", ",");
		log.info("set {}", prettyWrite(objectMapper, stringSet));
		assertThat(stringSet.size(), equalTo(0));
	}

	@Test
	void testLocateEnum() {
		
		TestEnum value = ApiUtils.locateEnum(TestEnum.class, "FIRST", TestEnum.SECOND);
		log.info("{}", value);
		assertThat(value, equalTo(TestEnum.FIRST));

		value = ApiUtils.locateEnum(TestEnum.class, "UNKNOWN", TestEnum.SECOND);
		assertThat(value, equalTo(TestEnum.SECOND));
	}

	@Test
	void testCloneEntity() {
		
		EntityModule entity = new EntityModule();
		entity.setBigDecimal(BigDecimal.valueOf(1.0D));
		entity.setDateVal(OffsetDateTime.now());
		entity.setId("1234");
		entity.setNumberVal1(9876);
		entity.setNumberVal2(8765);
		entity.setStringVal("somevalue");

		EntityModule cloneEntity = ApiUtils.cloneEntity(entity);
		log.info("clone \n{} \n{}", entity, cloneEntity);
		assertEquals(entity, cloneEntity);

		assertThat(entity.getId(), equalTo(cloneEntity.getId()));

	}

	@Test
	void testCloneEntity_withNewEntity() {
		
		EntityModule entity = new EntityModule();
		entity.setBigDecimal(BigDecimal.valueOf(1.0D));
		entity.setDateVal(OffsetDateTime.now());
		entity.setId("1234");
		entity.setNumberVal1(9876);
		entity.setNumberVal2(8765);
		entity.setStringVal("somevalue");

		EntityModule cloneEntity = ApiUtils.cloneEntity(new EntityModule(), entity);
		assertEquals(entity, cloneEntity);

		assertThat(entity.getId(), equalTo(cloneEntity.getId()));

	}

	@Test
	void testProcessElements() {
		
		List<EntityModule> entityList = new ArrayList<>();

		EntityModule entity = new EntityModule();
		entity.setBigDecimal(BigDecimal.valueOf(1.0D));
		entity.setDateVal(OffsetDateTime.now());
		entity.setId("1");
		entity.setNumberVal1(1L);
		entity.setNumberVal2(1.1D);
		entity.setStringVal("somevalue");

		entityList.add(entity);

		entity = new EntityModule();
		entity.setBigDecimal(BigDecimal.valueOf(2.0D));
		entity.setDateVal(OffsetDateTime.now());
		entity.setId("2");
		entity.setNumberVal1(2L);
		entity.setNumberVal2(2.2D);
		entity.setStringVal("somevalue");

		entityList.add(entity);

		List<String> newList = new ArrayList<>();

		ApiUtils.processElements(entityList, e -> ((Long) e.getNumberVal1()) > 1L, e -> "test" + e.getId(), v -> newList.add(v));

		log.info("{}", ApiUtils.prettyWrite(objectMapper, newList));
		assertThat(newList.get(0), equalTo("test2"));
	}

	@Test
	void testConvertLongValue() {
		
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("test1", "1234");
		paramMap.put("test2", "12345L");
		paramMap.put("test3", "ABCD");

		Long result = extractLongValue(paramMap, "test1", 0L);
		log.info("{}", result);
		assertThat(result, equalTo(1234L));

		result = extractLongValue(paramMap, "test2", null);
		log.info("{}", result);
		assertNull(result);

		result = extractLongValue(paramMap, "test3", null);
		log.info("{}", result);
		assertNull(result);

	}

	@Test
	void test_validateParamsMap() {
		
		Map<String, String> allParams = new HashMap<>();
		Map<String, String> headers = new HashMap<>();

		allParams.put(" key1 ", "value");
		headers.put("env", "it01");

		validateParamsMap(allParams);

		log.info("allParams {}", prettyWrite(objectMapper, allParams));

		String[] profiles = { "it01", "nonProd" };
		validateParamsMap(headers, allParams, profiles);

		log.info("headers {}", prettyWrite(objectMapper, headers));
		assertThat(headers.get("env"), equalTo("it01"));
		assertThat(headers.get(X_RUNTIME_ENV.value()), equalTo("it01,nonProd"));

		validateParamsMap(headers, allParams, "np", "pr");
		log.info("headers {}", prettyWrite(objectMapper, headers));
		assertThat(headers.get("env"), equalTo("it01"));

		headers.clear();
		validateParamsMap(headers, allParams, "np", "pr");
		log.info("headers {}", prettyWrite(objectMapper, headers));
		assertThat(headers.get("env"), equalTo(null));
		assertThat(headers.get(X_RUNTIME_ENV.value()), equalTo("np,pr"));

		headers.clear();
		validateParamsMap(headers, allParams, "np", "prx");
		log.info("headers {}", prettyWrite(objectMapper, headers));
		assertThat(headers.get(X_RUNTIME_ENV.value()), equalTo("np,prx"));

	}

	@Test
	void test_buildResponseEntity_status206() {
		
		List<EntityModule> entityList = new ArrayList<>();
		entityList.add(EntityModule.builder().id("1").build());
		entityList.add(EntityModule.builder().id("2").build());
		entityList.add(EntityModule.builder().id("3").build());

		ResponseEntity<List<EntityModule>> result = ApiUtils.buildResponseEntity(entityList, 10L, singletonMap(ENV_STRING.value(), "it01"), Collections.emptyMap());
		log.info("{}", prettyWrite(objectMapper, result));

		assertTrue(result.getHeaders().get(ENV_STRING.value()).stream().anyMatch(env -> env.equals("it01")));
		assertTrue(result.getHeaders().get(CommonConstantEnum.X_TOTAL_COUNT.value()).stream().anyMatch(count -> count.equals("10")));
		assertTrue(result.getHeaders().get(CommonConstantEnum.X_RESULT_COUNT.value()).stream().anyMatch(count -> count.equals("3")));
		assertThat(result.getStatusCodeValue(), equalTo(206));

	}

	@Test
	void test_buildResponseEntity_status200() {
		
		List<EntityModule> entityList = new ArrayList<>();
		entityList.add(EntityModule.builder().id("1").build());
		entityList.add(EntityModule.builder().id("2").build());
		entityList.add(EntityModule.builder().id("3").build());
		Map<String, String> headers = new HashMap<>();
		headers.put("env", "pr");
		headers.put(CommonConstantEnum.X_RUNTIME_ENV.value(), "it02,non-pr");

		ResponseEntity<List<EntityModule>> result = ApiUtils.buildResponseEntity(entityList, 3L, headers);
		log.info("{}", prettyWrite(objectMapper, result));

		assertTrue(result.getHeaders().get(ENV_STRING.value()).stream().anyMatch(env -> env.equals("pr")));
		assertTrue(result.getHeaders().get(X_RUNTIME_ENV.value()).stream().anyMatch(env -> env.equals("it02,non-pr")));
		assertTrue(result.getHeaders().get(CommonConstantEnum.X_TOTAL_COUNT.value()).stream().anyMatch(count -> count.equals("3")));
		assertTrue(result.getHeaders().get(CommonConstantEnum.X_RESULT_COUNT.value()).stream().anyMatch(count -> count.equals("3")));
		assertThat(result.getStatusCodeValue(), equalTo(200));
	}

	@Test
	void test_buildResponseEntity_body() {
		
		EntityModule entity = EntityModule.builder().id("1").build();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(ENV_STRING.value(), "local");
		headers.put("someHeader", "someValue");
		headers.put(X_RUNTIME_ENV.value(), "local,non-prod");

		ResponseEntity<EntityModule> result = ApiUtils.buildResponseEntity(entity, headers, null);

		log.info("{}", prettyWrite(objectMapper, result));

		assertThat(result.getStatusCodeValue(), equalTo(200));
		assertTrue(result.getHeaders().get(ENV_STRING.value()).stream().anyMatch(env -> env.equals("local")));
		assertTrue(result.getHeaders().get(CommonConstantEnum.X_TOTAL_COUNT.value()).stream().anyMatch(count -> count.equals("1")));
		assertTrue(result.getHeaders().get(CommonConstantEnum.X_RESULT_COUNT.value()).stream().anyMatch(count -> count.equals("1")));

		Map<String, String> params = new HashMap<>();
		params.put(CommonConstantEnum.CACHE_CONTROL.value(), "maxAge=600, must-revalidate");
		headers.put(ENV_STRING.value(), null);
		result = ApiUtils.buildResponseEntity(entity, headers, params);

		log.info("{}", prettyWrite(objectMapper, result));
	}

	@Test
	void test_buildResponseEntity_nullEnv() {
		
		EntityModule entity = EntityModule.builder().id("1").build();

		ResponseEntity<EntityModule> result = ApiUtils.buildResponseEntity(entity, null, null);
		log.info("{}", prettyWrite(objectMapper, result));

		assertThat(result.getStatusCodeValue(), equalTo(200));
		assertNull(result.getHeaders().get(ENV_STRING.value()));
	}

	@Test
	void test_setifNotNull() {
		
		EntityModule entity = new EntityModule();
		log.info("id {}", prettyWrite(objectMapper, entity.getId()));

		ApiUtils.setIfNonNull(entity::setId, "abcd");

		log.info("id {}", prettyWrite(objectMapper, entity.getId()));
		assertThat(entity.getId(), equalTo("abcd"));

		ApiUtils.setIfNonNull(entity::setId, String::toUpperCase, "abcd");
		log.info("id {}", prettyWrite(objectMapper, entity.getId()));
		assertThat(entity.getId(), equalTo("ABCD"));

		ApiUtils.setIfNonNull(entity::setNumberVal1, Integer::parseInt, "1234");
		log.info("val1 {}", prettyWrite(objectMapper, entity.getNumberVal1()));
		assertThat(entity.getNumberVal1(), equalTo(1234));

		ApiUtils.setIfNonNull(entity::setNumberVal1, this::extractInteger, "abcd");
		log.info("val1 {}", prettyWrite(objectMapper, entity.getNumberVal1()));
		assertThat(entity.getNumberVal1(), equalTo(0));

		NumberFormatException ex = assertThrows(NumberFormatException.class, () -> ApiUtils.setIfNonNull(entity::setNumberVal1, Integer::parseInt, "abcd"));
		log.info("integer parsing error : [{}]", ex.getLocalizedMessage());

	}

	private Integer extractInteger(String value) {
		
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	@Test
	void testBuildRequestUriString() {
		
		Map<String, String> headers = Map.of("key1", "value1", "key2", "value2");
		String result = ApiUtils.buildRequestUriString(headers, "/tester/");

		log.info("result {}", result);
		assertNotNull(result);
	}

	@Test
	void testValidateSecretVariable() {
		
		ValidationException exception = assertThrows(ValidationException.class, () -> ApiUtils.validateSecretVariable("${SOME_ID}"));
		log.info("validationException expected [{}]", exception.getMessage());

		exception = assertThrows(ValidationException.class, () -> ApiUtils.validateSecretVariable("${sm://USER_ID}"));
		log.info("validationException expected [{}]", exception.getMessage());

		assertEquals("some_value", ApiUtils.validateSecretVariable("some_value"));
	}

}