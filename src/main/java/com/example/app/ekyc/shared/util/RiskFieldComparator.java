package com.example.app.ekyc.shared.util;

import com.example.app.ekyc.shared.annotation.TriggerRiskEvaluation;
import java.lang.reflect.Field;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RiskFieldComparator {

    /**
     * Compares the annotated critical fields of two instances.
     * Returns true if any field marked with @TriggerRiskEvaluation differs between them.
     */
    public static boolean hasCriticalFieldChanged(Object original, Object updated) {
        if (original == null && updated == null) {
            return false;
        }
        if (original == null || updated == null) {
            return true;
        }
        if (!original.getClass().equals(updated.getClass())) {
            log.warn("Attempting to compare different classes: {} and {}", original.getClass(), updated.getClass());
            return false;
        }

        Class<?> clazz = original.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(TriggerRiskEvaluation.class)) {
                field.setAccessible(true);
                try {
                    Object val1 = field.get(original);
                    Object val2 = field.get(updated);
                    if (!Objects.equals(val1, val2)) {
                        log.debug("Critical change detected in field '{}': {} -> {}", field.getName(), val1, val2);
                        return true;
                    }
                } catch (IllegalAccessException e) {
                    log.error("Failed to read field '{}' for risk comparison", field.getName(), e);
                    throw new RuntimeException("Risk assessment field comparison failed", e);
                }
            }
        }
        return false;
    }
}
