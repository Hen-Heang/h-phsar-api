package com.henheang.hphsar.model.practice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * P1 practice: Bean Validation rules on {@link PracticeBoardInVO} paging fields.
 */
class PracticeBoardInVOTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    private Set<ConstraintViolation<PracticeBoardInVO>> validatePageSize(int recordCountPerPage) {
        PracticeBoardInVO inVO = new PracticeBoardInVO();
        inVO.setRecordCountPerPage(recordCountPerPage);
        return validator.validate(inVO);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 100})
    void recordCountPerPage_withinLimit_isValid(int recordCountPerPage) {
        assertThat(validatePageSize(recordCountPerPage)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 101, 1_000_000})
    void recordCountPerPage_outsideLimit_isRejected(int recordCountPerPage) {
        assertThat(validatePageSize(recordCountPerPage))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("recordCountPerPage");
    }
}
