package org.eu.liuhw.http.file.sync.base.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author JavierHouse
 */
@Target({FIELD, METHOD})
@Retention(RUNTIME)
@Constraint(validatedBy = ConditionalNotNullValidator.class)
public @interface ConditionalNotNull {
    String message() default "字段不能为空";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String field();          // 关联字段名
    String expectedValue();  // 触发条件值
}