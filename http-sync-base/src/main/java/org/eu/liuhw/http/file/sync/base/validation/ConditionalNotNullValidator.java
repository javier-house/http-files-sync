package org.eu.liuhw.http.file.sync.base.validation;

import cn.hutool.core.bean.BeanUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

/**
 * @author JavierHouse
 */
public class ConditionalNotNullValidator  implements ConstraintValidator<ConditionalNotNull, Object> {
    private String field;
    private String expectedValue;

    @Override
    public void initialize(ConditionalNotNull constraint) {
        this.field = constraint.field();
        this.expectedValue = constraint.expectedValue();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        if (object == null) {
            // 获取关联字段值
            Object fieldValue = BeanUtil.getProperty(object, field);
            if (Objects.isNull(fieldValue) || expectedValue.equals(fieldValue.toString())) {
                return false;  // 条件触发时，当前字段不可为空
            }
        }
        return true;
    }
}
