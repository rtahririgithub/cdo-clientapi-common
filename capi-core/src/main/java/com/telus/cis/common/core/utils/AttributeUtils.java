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

import static com.telus.cis.common.core.utils.ApiUtils.getAllFields;
import static java.util.Arrays.asList;
import static java.util.function.Function.identity;
import static org.apache.commons.lang3.StringUtils.SPACE;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.telus.cis.common.core.domain.AttributeFilter;
import com.telus.cis.common.core.domain.FilterOperatorEnum;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public final class AttributeUtils
{
    @Getter
    @Builder
    private static class ParsingEntity {
        private String attribute;
        private String operation;
        private String value;
    }


    /*
     * define operation from tmforum to SPEL operation
     */
    private enum OperationEnum {
        EQ( asList( "=", ".eq", "%3D%3D" ) ),
        GT( asList( ">", ".gt", "%3E") ),
        GE( asList( ">=", ".gte", "%3E%3D") ),
        LT( asList( "<", ".lt", "%3C") ),
        LE( asList( "<=", ".lte", "%3C%3D") ),
        NE( asList( "!=", ".ne", "!%3D")  ),
        MATCHES( asList( "*=", "regex", "%3D~" ) );

        @Getter
        private List<String> alias;

        OperationEnum(List<String> alias) {
            this.alias = alias;
        }
    }


    private static final Set<String> booleanSet = new HashSet<>( asList( "true", "false", "null" ) );


    private AttributeUtils() {}


    public static String spelExtressionString(List<Field> fieldList, Map<String,String> allParams)
    {
        List<ParsingEntity> parsingEntities = new ArrayList<>();
        allParams.entrySet().removeIf( param -> {
            ParsingEntity entity = mapParameterToEntity( fieldList, param );
            return Objects.isNull( entity ) ? false : parsingEntities.add( entity );
        } );
        return parsingEntities.stream()
                .map(e -> new StringBuilder( "( ").append( e.getAttribute() ).append( SPACE )
                        .append( e.getOperation() ).append( SPACE )
                        .append( e.getValue() ).append( " )" ).toString() )
                .collect( Collectors.joining( " OR " ) );
    }


    private static ParsingEntity mapParameterToEntity(List<Field> fieldList, Entry<String, String> param)
    {
        for( OperationEnum oper : OperationEnum.values() ) {
            for ( String alias : oper.getAlias() ) {
                ParsingEntity entity = parseAttributeByItem( fieldList, alias, oper.name(), param.getKey().trim(), param.getValue() );
                if ( Objects.nonNull( entity ) ) {
                    return entity;
                }
            }
        }
        return null;
    }


    private static ParsingEntity parseAttributeByItem(List<Field> fieldList, String alias, String oper, String attribute, String value)
    {
        if ( ! StringUtils.endsWith( attribute, alias ) ) {
            return null;
        }
        String param = attribute.substring(0, attribute.length() - alias.length());
        String fieldName = StringUtils.split( param, ".", 2 )[0];
        for( Field field : fieldList ) {
            if ( fieldName.equals( field.getName() ) ) {
                return new ParsingEntity( param, oper,
                        ( containAllTimeInstant( field ) ) ? getInstantValue( value ) : value );
            }
        }
        return null;
    }


    /**
     * expecting ISO format: yyyy-mm-ddTHH:DD:MMZ
     */
    private static String getInstantValue(String value)
    {
        if ( value.equalsIgnoreCase( "null" ) ) {
            return value;
        }
        StringBuilder strBuilder = new StringBuilder( "T(java.time.OffsetDateTime).parse('");
        return ( value.length() > 10 ) ? strBuilder.append( value ).append( "')" ).toString() :
                strBuilder.append(value).append( "T00:00:00Z')" ).toString();
    }


    public static boolean containAllTimeInstant(Field field)
    {
        if ( Temporal.class.isAssignableFrom( field.getType() ) ) {
            return true;
        }
        Class<?> fieldClass =  Collection.class.isAssignableFrom( field.getType() ) ?
              (Class<?>)( ( ( ParameterizedType )field.getGenericType() ).getActualTypeArguments()[0] ) : field.getType();
        List<Field> fields = Stream.of( fieldClass.getDeclaredFields() )
              .filter( f -> ! Modifier.isStatic(f.getModifiers() ) )
              .collect( Collectors.toList() );

        return ( fields.size() <= 1 ) ? false :
             fields.stream().allMatch( f -> Temporal.class.isAssignableFrom( f.getType() ) );
    }


    public static Map<String, Field> prepareAttributeMap( Class<?> classType ) {
        return getAllFields( classType ).stream().collect( Collectors.toMap( Field::getName, identity() ) );
    }


    public static List< Pair<Field, String> > createAttributeList(Class<?> classType, Map<String, String> paramMap)
    {
        Map<String, Field> attributeMap = prepareAttributeMap( classType );

        List< Pair<Field, String> > attributeList = new ArrayList<>();
        paramMap.forEach( ( key, value ) -> {
            Field field = attributeMap.get( key );
            if ( field != null ) {
                Pair<Field, String> attributPair = validateValue(field, value);
                if ( attributPair != null ) {
                    attributeList.add( attributPair );
                }
            }
        } );
        return attributeList;
    }


    /**
     * This method is used to extract the proper "field" attributes from a REST query parameter list and a TMF resource. TMF "field"
     * attributes are used to allow generic filtering on any TMF resource attribute. This method will only return attributes that match
     * the TMF resource in the query parameter list and provide a high level validation on those filtering values.
     *
     * @param responseClassType
     * @param paramMap
     * @return
     */
    public static List<Pair<Field, AttributeFilter>> extractAttributeFilters(Class<?> responseClassType, Map<String, String> paramMap) {
        List<Pair<Field, AttributeFilter>> attributeFilterList = new ArrayList<>();

        paramMap.forEach((key, value) -> {
            Pair<String, String> attNameOperatorPair = parseParamKey(key);
            String attributeName = attNameOperatorPair.getLeft();
            String operator = attNameOperatorPair.getRight();
            AttributeFilter attributeFilter = createAttributeFilter(attributeName, value, operator);

            Field field = getField(responseClassType, new ArrayList<>(asList(attributeName.split("\\."))));

            if (field != null && isValidFilterValue(field, attributeFilter)) {
                attributeFilterList.add(new ImmutablePair<>(field, attributeFilter));
            }
        });

        return attributeFilterList;
    }


    /**
     * Parse the specified parameter key to get attribute name (e.g., relatedParaty.fullname) and operator (e.g., .regex)
     * @param paramKey the specified query parameter key (e.g., relatedParaty.fullname.regex)
     * @return pair of attribute name and operator
     */
    private static Pair<String, String> parseParamKey(String paramKey) {
        String attributeName = null;
        String operator = null;

        boolean hasOperator = Arrays.stream(FilterOperatorEnum.values()).anyMatch(value -> paramKey.toLowerCase().endsWith(value.value()));

        if (hasOperator) {
            operator = paramKey.substring(paramKey.lastIndexOf("."));
            attributeName = paramKey.substring(0, paramKey.indexOf(operator));
            operator = operator.toLowerCase();
        } else {
            attributeName = paramKey;
            operator = FilterOperatorEnum.EQ.value();
        }
        return new ImmutablePair<>(attributeName, operator);
    }


    private static AttributeFilter createAttributeFilter(String attributeName, String value, String operator) {
        AttributeFilter attributeFilter = new AttributeFilter();
        attributeFilter.setAttributeName(attributeName);
        attributeFilter.setFilterValue(value);

        if (!StringUtils.isEmpty(operator)) {
            attributeFilter.setOperator(asList(FilterOperatorEnum.values()).stream()
                    .filter(enumValue -> operator.equalsIgnoreCase(enumValue.value()))
                    .findFirst()
                    .orElse(null));
        } else {
            attributeFilter.setOperator(FilterOperatorEnum.EQ);
        }
        return attributeFilter;
    }


    /**
     * Get the field of the specified class
     * @param responseClassType the specified class which has the field
     * @param attributeList the specified attribute name list, e.g., [relatedParty, fullname]
     * @return the instance of Field with the specified attribute name, e.g., fullName
     */
    private static Field getField(Class<?> responseClassType, List<String> attributeList) {
        int size = attributeList.size();
        Map<String, Field> attributeMap = prepareAttributeMap(responseClassType);
        Field field = attributeMap.get(attributeList.get(0));

        if (size == 1) {
            if (field != null && isForFiltering(field)) {
                return field;
            }
            log.warn("Attribute name {}", attributeList.get(0) + " is invalid for multi-level filtering.");
            return null;
        } else if (field == null) {
            log.warn( "Invalid attribute name {}", attributeList.get(0) );
            return null;
        } else if (Collection.class.isAssignableFrom(field.getType())) {
            Class<?> fieldArgClass = null;
            Type gType = field.getGenericType();

            if(gType instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) gType;
                Type[] fieldArgTypes = pType.getActualTypeArguments();
                if (!ArrayUtils.isEmpty(fieldArgTypes)) {
                    fieldArgClass = (Class<?>) fieldArgTypes[0];
                }
            }

            if (fieldArgClass != null) {
                attributeList.remove(0);
                return getField(fieldArgClass, attributeList);
            }

            return null;
        } else {
            attributeList.remove(0);
            return getField(field.getType(), attributeList);
        }
    }


    private static boolean isValidFilterValue(Field field, AttributeFilter attributeFilter)
    {
        if ( ClassUtils.isAssignable( Boolean.class, field.getType() ) ) {
            for ( String booleanValue : attributeFilter.getFilterValue().split( "," ) ) {
                if ( ! booleanSet.contains( booleanValue.trim() ) ) {
                    log.warn( "invalid boolean variable {}", attributeFilter.getFilterValue() );
                  return false;
                }
            }
        }
        return true;
    }


    private static Pair<Field, String> validateValue(Field field, String value)
    {
        if ( ClassUtils.isAssignable( Boolean.class, field.getType() ) ) {
            for ( String booleanValue : value.split( "," ) ) {
                if ( ! booleanSet.contains( booleanValue.trim() ) ) {
                    log.warn( "invalid boolean variable {}", value );
                  return null;
                }
            }
        }
        return new ImmutablePair<>( field, value );
    }


    /**
     * add items that matches the criteria , objectList reference update
     */
    public static <T> List<T> applyAttributeFilterList(List<T> objectList, Class<?> classType, Map<String, String> paramMap)
    {
        return applyAttributeFilterList( objectList, createAttributeList(classType, paramMap) );
    }


    public static <T> List<T> applyAttributeFilterList(List<T> objectList, List< Pair<Field, String> > attributeList)
    {
        return objectList.stream().filter( obj -> filterAttribute( obj, attributeList ) ).collect( Collectors.toList() );
    }


    /**
     * remove items that does not match, objectList reference unchanged
     */
    public static <T> void applyAttributeFilters(List<T> objectList, Class<?> classType, Map<String, String> paramMap)
    {
        applyAttributeFilters( objectList, createAttributeList(classType, paramMap) );
    }


    public static <T> void applyAttributeFilters(List<T> objectList, List< Pair<Field, String> > attributeList)
    {
        if ( CollectionUtils.isNotEmpty( attributeList ) ) {
            objectList.removeIf( object -> noneMatchAttribute( object, attributeList ) );
        }
    }

    /**
     * This method will remove objects that should be filtered out from the list based on a list of filtering criteria that has been
     * passed through the query parameters of a TMF find resource operation. The filtering criteria is passed into the operation as
     * a list of Field and AttributeFilter pairs that can be extracted from a list of parameters using the <i>extractAttributeFilters</i>
     * method in this same class.
     * @param <T>
     * @param Object
     * @param attributeList
     */
    public static <T> void applyMultiLevelFiltering(List<T> objectList, List<Pair<Field, AttributeFilter>> attributeList)
    {
        if ( CollectionUtils.isNotEmpty( attributeList ) ) {
            objectList.removeIf( object -> noneMatchAttributeEnhanced( object, attributeList ) );
        }
    }

    public static <T> boolean filterAttribute(T object, List<Pair<Field, String>> attributeList)
    {
        return attributeList.stream().anyMatch( attribute -> matchAttribute( object, attribute ) );
    }


    private static <T> boolean matchAttribute(T object, Pair<Field, String> pair)
    {
        boolean matched = false;
        if ( ObjectUtils.allNotNull( object, pair, pair.getRight() ) ) {
            if ( ClassUtils.isAssignable( Boolean.class, pair.getLeft().getType(), false ) ) {
                matched = matchBooleanValue( object, pair );
            }
            else if ( ClassUtils.isAssignable( Number.class, pair.getLeft().getType(), false ) ) {
                matched = matchNumberValue( object, pair );
            }
            else if ( Enum.class.isAssignableFrom( pair.getLeft().getType() ) ) {
                matched = matchEnumValue( object, pair );
            }
            else {
                matched = matchStringValue( object, pair );
            }
        }
        return matched;
    }

    private static <T> boolean matchAttributeEnhanced(T object, Pair<Field, AttributeFilter> pair)
    {
        boolean matched = false;
        if (ObjectUtils.allNotNull(object, pair, pair.getRight())) {
            if (ClassUtils.isAssignable(pair.getLeft().getType(), Boolean.class, true)) {
                matched = matchBooleanValueEnhanced(object, pair);
            }
            else if (ClassUtils.isAssignable(pair.getLeft().getType(), Number.class, true)) {
                matched = matchNumberValueEnhanced(object, pair);
            }
            else if (Enum.class.isAssignableFrom( pair.getLeft().getType())) {
                matched = matchEnumValueEnhanced(object, pair);
            }
            else {
                matched = matchStringValueEnhanced(object, pair);
            }
        }
        return matched;
    }

    private static <T> boolean noneMatchAttribute(T object, List<Pair<Field, String>> attributeList)
    {
        boolean remove = true;
        if ( attributeList.stream().anyMatch( attribute -> matchAttribute( object, attribute ) ) ) {
            remove = false;
        }
        return remove;
    }

    private static <T> boolean noneMatchAttributeEnhanced(T object, List<Pair<Field, AttributeFilter>> attributeList)
    {
        boolean remove = true;
        if ( attributeList.stream().anyMatch( attribute -> matchAttributeEnhanced( object, attribute ) ) ) {
            remove = false;
        }
        return remove;
    }

    private static <T> boolean  matchBooleanValue(T object, Pair<Field, String> pair)
    {
        String [] values = pair.getRight().split( "," );
        try {
            Boolean objectValue  = ( Boolean )PropertyUtils.getProperty( object, pair.getLeft().getName() );
            for ( String value : values ) {
                value = value.trim();
                Boolean booleanValue = "null".equalsIgnoreCase( value ) ? null : Boolean.valueOf( value );
                if ( Objects.isNull( objectValue ) ) {
                    if ( Objects.isNull( booleanValue ) ) {
                        return true;
                    }
                }
                else {
                    if ( objectValue.equals( booleanValue ) ) {
                        return true;
                    }
                }
            }
        }
        catch ( IllegalAccessException | InvocationTargetException | NoSuchMethodException e ) {
            log.warn( "boolean get properties {} - {} {}", e.getClass().getSimpleName(), e.getLocalizedMessage() );
            log.trace( "Boolean Match Exception", e );
        }
        return false;
    }

    private static <T> boolean  matchBooleanValueEnhanced(T object, Pair<Field, AttributeFilter> pair)
    {
        String [] values = pair.getRight().getFilterValue().split( "," );
        try {
            Boolean objectValue  = ( Boolean )PropertyUtils.getProperty( object, pair.getLeft().getName() );
            for ( String value : values ) {
                value = value.trim();
                Boolean booleanValue = "null".equalsIgnoreCase( value ) ? null : Boolean.valueOf( value );
                if ( Objects.isNull( objectValue ) ) {
                    if ( Objects.isNull( booleanValue ) ) {
                        return true;
                    }
                }
                else {
                    if ( objectValue.equals( booleanValue ) ) {
                        return true;
                    }
                }
            }
        }
        catch ( IllegalAccessException | InvocationTargetException | NoSuchMethodException e ) {
            log.warn( "boolean get properties {} - {} {}", e.getClass().getSimpleName(), e.getLocalizedMessage() );
            log.trace( "Boolean Match Exception", e );
        }
        return false;
    }

    private static <T> boolean  matchNumberValue(T object, Pair<Field, String> pair)
    {
        String [] values = pair.getRight().split( "," );
        try {
            Number objectValue = ( Number )PropertyUtils.getProperty( object, pair.getLeft().getName() );
            for ( String value : values ) {
                Number numberValue = NumberUtils.createNumber( value.trim() );
                if ( Objects.isNull( objectValue ) ) {
                    if ( "null".equals( value ) ) {
                        return true;
                    }
                }
                else {
                    if ( objectValue.equals( numberValue ) ) {
                        return true;
                    }
                }
            }
        }
        catch ( IllegalAccessException | InvocationTargetException | NoSuchMethodException e ) {
            log.warn( "number get properties {} - {}", e.getClass().getSimpleName(), e.getLocalizedMessage() );
            log.trace( "Number Match Exception", e );
        }
        return false;
    }

    private static <T> boolean matchNumberValueEnhanced(T object, Pair<Field, AttributeFilter> pair)
    {
        FilterOperatorEnum operator = pair.getRight().getOperator();
        String attributeName = pair.getRight().getAttributeName();
        List<String> filterValues = new ArrayList<>(asList(pair.getRight().getFilterValue().split( "," )));
        List<String> names = new ArrayList<>(asList(attributeName.split( "\\." )));

        return matchNumberValue(object, names, filterValues, operator);
    }

    private static <T> boolean matchNumberValue(T object, List<String> names, List<String> filterValues, FilterOperatorEnum operator) {
        try {
            Object propertyObj = PropertyUtils.getProperty(object, names.get(0));

            if (Objects.isNull(propertyObj)) {
                if (names.size() == 1) {
                    for (String filterValue : filterValues) {
                        if ("null".equals(filterValue)) {
                            return true;
                         }
                    }
                    return false;
                } else {
                    return false;
                }
            }

            if (names.size() == 1) {
                Number propertyValue = (Number)propertyObj;
                for ( String filterValue : filterValues ) {
                    filterValue = filterValue.trim();
                    FilterOperatorEnum operatorEnum = ApiUtils.locateEnum( FilterOperatorEnum.class, filterValue.trim(), FilterOperatorEnum.NF );
                    switch ( operatorEnum ) {
                        case EQ :
                            return propertyValue.doubleValue() == Double.parseDouble(filterValue);
                        case GT :
                            return propertyValue.doubleValue() > Double.parseDouble(filterValue);
                        case GE :
                            return propertyValue.doubleValue() >= Double.parseDouble(filterValue);
                        case LT :
                            return propertyValue.doubleValue() < Double.parseDouble(filterValue);
                        case LE :
                            return propertyValue.doubleValue() <= Double.parseDouble(filterValue);
                        case NE :
                            return propertyValue.doubleValue() != Double.parseDouble(filterValue);
                        default:
                            return false;
                    }
                }
            } else if (Collection.class.isAssignableFrom(propertyObj.getClass())) {
                Iterator<?> itr = ((Iterable<?>)propertyObj).iterator();
                while(itr.hasNext()) {
                    names.remove(0);
                    Object elementObj = itr.next();
                    if (matchNumberValue(elementObj, names, filterValues, operator)) {
                        return true;
                    }
                }
            } else {
                names.remove(0);
                return matchNumberValue(propertyObj, names, filterValues, operator);
            }
        } catch (Exception e) {
            log.warn("matchNumberValue {} - {}", e.getClass().getSimpleName(), e.getLocalizedMessage());
            log.trace("Number Match Exception", e);
        }
        return false;
    }

    private static <T> boolean matchEnumValue(T object, Pair<Field, String> pair)
    {
        String [] values = pair.getRight().split( "," );
        try {
            Enum<?> objectValue = (Enum<?>) PropertyUtils.getProperty( object, pair.getLeft().getName() );
            for ( String value : values ) {
                if ( ( objectValue.name().equalsIgnoreCase( value ) ) ||
                     ( objectValue.toString().equalsIgnoreCase( value ) ) ) {
                    return true;
                }
            }
        }
        catch ( IllegalAccessException | InvocationTargetException | NoSuchMethodException e ) {
            log.warn( "get enum properties {} - {}", e.getClass().getSimpleName(), e.getLocalizedMessage() );
            log.trace( "enum Match Exception", e );
        }
        return false;
    }

    private static <T> boolean matchEnumValueEnhanced(T object, Pair<Field, AttributeFilter> pair)
    {
        String [] values = pair.getRight().getFilterValue().split( "," );
        try {
            Enum<?> objectValue = (Enum<?>) PropertyUtils.getProperty( object, pair.getLeft().getName() );
            for ( String value : values ) {
                if ( ( objectValue.name().equalsIgnoreCase( value ) ) ||
                     ( objectValue.toString().equalsIgnoreCase( value ) ) ) {
                    return true;
                }
            }
        }
        catch ( IllegalAccessException | InvocationTargetException | NoSuchMethodException e ) {
            log.warn( "get enum properties {} - {}", e.getClass().getSimpleName(), e.getLocalizedMessage() );
            log.trace( "enum Match Exception", e );
        }
        return false;
    }

    private static <T> boolean matchStringValue(T object, Pair<Field, String> pair)
    {
        String [] values = pair.getRight().split( "," );
        try {
            String stringValue = (String) PropertyUtils.getProperty( object, pair.getLeft().getName() );
            for ( String value : values ) {
                value = value.trim();
                if ( Objects.isNull( stringValue ) ) {
                    if ( "null".equals( pair.getRight() ) ) {
                        return true;
                    }
                }
                else {
                    if ( stringValue.equalsIgnoreCase( value ) ) {
                        return true;
                    }
                }

            }
        }
        catch ( IllegalAccessException | InvocationTargetException | NoSuchMethodException e ) {
            log.warn( "get properties {} - {}", e.getClass().getSimpleName(), e.getLocalizedMessage() );
            log.trace( "String Match Exception", e );
        }
        return false;
    }

    private static <T> boolean matchStringValueEnhanced(T object, Pair<Field, AttributeFilter> pair)
    {
        FilterOperatorEnum operator = pair.getRight().getOperator();
        String attributeName = pair.getRight().getAttributeName();
        List<String> filterValues = new ArrayList<>(asList(pair.getRight().getFilterValue().split( "," )));
        List<String> names = new ArrayList<>(asList(attributeName.split( "\\." )));

        return matchStringValue(object, names, filterValues, operator);
    }

    private static <T> boolean matchStringValue(T object, List<String> names, List<String> filterValues, FilterOperatorEnum operator) {
        try {
            Object propertyObj = PropertyUtils.getProperty(object, names.get(0));

            if (Objects.isNull(propertyObj)) {
                if (names.size() == 1) {
                    for (String filterValue : filterValues) {
                        if ("null".equals(filterValue)) {
                            return true;
                         }
                    }
                    return false;
                } else {
                    return false;
                }
            }

            if (names.size() == 1) {
                String propertyValue = (String)propertyObj;
                for ( String filterValue : filterValues ) {
                    filterValue = filterValue.trim();
                    if (FilterOperatorEnum.REGEX.value().equals(operator.value())) {
                        // We only support single regular expression, e.g., relatedParty.fullName.regex=.*John.*%7C.*Smith.*
                        return Pattern.compile(filterValue, Pattern.CASE_INSENSITIVE).matcher(propertyValue).find();
                    } else {
                        if (propertyValue.equalsIgnoreCase(filterValue)) {
                            return true;
                        }
                    }
                }
            } else if (Collection.class.isAssignableFrom(propertyObj.getClass())) {
                Iterator<?> itr = ((Iterable<?>)propertyObj).iterator();
                while(itr.hasNext()) {
                    names.remove(0);
                    Object elementObj = itr.next();
                    if (matchStringValue(elementObj, names, filterValues, operator)) {
                        return true;
                    }
                }
            } else {
                names.remove(0);
                return matchStringValue(propertyObj, names, filterValues, operator);
            }
        } catch (Exception e) {
            log.warn( "matchStringValue {} - {}", e.getClass().getSimpleName(), e.getLocalizedMessage() );
            log.trace( "String Match Exception", e );
        }
        return false;
    }

    private static boolean isForFiltering(Field field) {
        if (ClassUtils.isAssignable(field.getType(), Boolean.class, false)) {
            return true;
        } else if (ClassUtils.isAssignable(field.getType(), Number.class, false)) {
            return true;
        } else if (ClassUtils.isAssignable(field.getType(), String.class, false)) {
            return true;
        } else if (ClassUtils.isAssignable(field.getType(), Date.class, false)) {
            return true;
        } else if (Enum.class.isAssignableFrom(field.getType())) {
            return true;
        } else {
            return false;
        }
    }
}
