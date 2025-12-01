package com.yimusi.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SequenceBizType 枚举测试类
 */
class SequenceBizTypeTest {

    @Nested
    @DisplayName("formatSequenceNo() 方法测试")
    class FormatSequenceNoTest {

        @Test
        @DisplayName("INSPECTION_DEVICE 在序列号范围内正常格式化")
        void inspectionDevice_should_formatCorrectly_when_sequenceInRange() {
            Long seqNo = 1234L;
            String result = SequenceBizType.INSPECTION_DEVICE.formatSequenceNo(seqNo);
            String expectedPrefix = "IND";
            String expectedSeqNo = "1234";
            String expectedDatePart = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

            String expected = expectedPrefix + expectedDatePart + expectedSeqNo;
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("INSPECTION_DEVICE 序列号超出长度时直接返回原始值")
        void inspectionDevice_should_returnOriginal_when_sequenceExceedsLength() {
            Long seqNo = 99999L;  // 超出4位长度
            String result = SequenceBizType.INSPECTION_DEVICE.formatSequenceNo(seqNo);
            String expectedPrefix = "IND";
            String expectedDatePart = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

            String expected = expectedPrefix + expectedDatePart + "99999";
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("PROJECT_INTERNAL 不限制长度，直接附加序列号")
        void projectInternal_should_formatWithoutMaxLength() {
            Long seqNo = 123456789L;
            String result = SequenceBizType.PROJECT_INTERNAL.formatSequenceNo(seqNo);
            String expected = "123456789";  // 没有前缀和日期
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("INSPECTION_DEVICE 四位序列号补零")
        void inspectionDevice_should_padWithZeros_forFourDigitSequence() {
            Long seqNo = 5L;
            String result = SequenceBizType.INSPECTION_DEVICE.formatSequenceNo(seqNo);
            String expectedPrefix = "IND";
            String expectedSeqNo = "0005";
            String expectedDatePart = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

            String expected = expectedPrefix + expectedDatePart + expectedSeqNo;
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("INSPECTION_DEVICE 边界值 0001 格式化")
        void inspectionDevice_should_padCorrectly_forOne() {
            Long seqNo = 1L;
            String result = SequenceBizType.INSPECTION_DEVICE.formatSequenceNo(seqNo);
            String expectedPrefix = "IND";
            String expectedSeqNo = "0001";
            String expectedDatePart = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

            String expected = expectedPrefix + expectedDatePart + expectedSeqNo;
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("INSPECTION_DEVICE 边界值 9999 格式化")
        void inspectionDevice_should_formatCorrectly_forMaxValue() {
            Long seqNo = 9999L;
            String result = SequenceBizType.INSPECTION_DEVICE.formatSequenceNo(seqNo);
            String expectedPrefix = "IND";
            String expectedSeqNo = "9999";
            String expectedDatePart = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

            String expected = expectedPrefix + expectedDatePart + expectedSeqNo;
            assertEquals(expected, result);
        }
    }

    @Nested
    @DisplayName("fromCode() 方法测试")
    class FromCodeTest {

        @Test
        @DisplayName("有效的编码应返回对应的枚举")
        void fromCode_should_returnEnum_forValidCode() {
            SequenceBizType result = SequenceBizType.fromCode("inspection_device");
            assertEquals(SequenceBizType.INSPECTION_DEVICE, result);
        }

        @Test
        @DisplayName("有效的 project_internal 编码应返回对应枚举")
        void fromCode_should_returnProjectInternal_forValidCode() {
            SequenceBizType result = SequenceBizType.fromCode("project_internal");
            assertEquals(SequenceBizType.PROJECT_INTERNAL, result);
        }

        @Test
        @DisplayName("不存在的编码应抛出 IllegalArgumentException")
        void fromCode_should_throwException_forInvalidCode() {
            assertThrows(IllegalArgumentException.class, () -> {
                SequenceBizType.fromCode("invalid_code");
            });
        }

        @Test
        @DisplayName("null 编码应抛出 IllegalArgumentException")
        void fromCode_should_throwException_forNullCode() {
            assertThrows(IllegalArgumentException.class, () -> {
                SequenceBizType.fromCode(null);
            });
        }

        @Test
        @DisplayName("空字符串编码应抛出 IllegalArgumentException")
        void fromCode_should_throwException_forEmptyCode() {
            assertThrows(IllegalArgumentException.class, () -> {
                SequenceBizType.fromCode("");
            });
        }
    }

    @Nested
    @DisplayName("findByCode() 方法测试")
    class FindByCodeTest {

        @Test
        @DisplayName("有效的编码应返回 Optional 包装的枚举值")
        void findByCode_should_returnOptionalWithValue_forValidCode() {
            Optional<SequenceBizType> result = SequenceBizType.findByCode("inspection_device");
            assertTrue(result.isPresent());
            assertEquals(SequenceBizType.INSPECTION_DEVICE, result.get());
        }

        @Test
        @DisplayName("无效的编码应返回 Optional.empty()")
        void findByCode_should_returnEmptyOptional_forInvalidCode() {
            Optional<SequenceBizType> result = SequenceBizType.findByCode("invalid_code");
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("null编码应返回 Optional.empty()")
        void findByCode_should_returnEmptyOptional_forNullCode() {
            Optional<SequenceBizType> result = SequenceBizType.findByCode(null);
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("空字符串编码应返回 Optional.empty()")
        void findByCode_should_returnEmptyOptional_forEmptyCode() {
            Optional<SequenceBizType> result = SequenceBizType.findByCode("");
            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("字段属性测试")
    class PropertyTest {

        @Test
        @DisplayName("INSPECTION_DEVICE 的所有字段值正确")
        void inspectionDevice_should_haveCorrectProperties() {
            assertEquals("inspection_device", SequenceBizType.INSPECTION_DEVICE.getCode());
            assertEquals("检测设备编号", SequenceBizType.INSPECTION_DEVICE.getDescription());
            assertEquals("IND", SequenceBizType.INSPECTION_DEVICE.getPrefix());
            assertEquals(4, SequenceBizType.INSPECTION_DEVICE.getSequenceLength());
            assertEquals(ResetStrategy.DAILY, SequenceBizType.INSPECTION_DEVICE.getResetStrategy());
        }

        @Test
        @DisplayName("PROJECT_INTERNAL 的所有字段值正确")
        void projectInternal_should_haveCorrectProperties() {
            assertEquals("project_internal", SequenceBizType.PROJECT_INTERNAL.getCode());
            assertEquals("项目内部序号", SequenceBizType.PROJECT_INTERNAL.getDescription());
            assertEquals("", SequenceBizType.PROJECT_INTERNAL.getPrefix());
            assertEquals(0, SequenceBizType.PROJECT_INTERNAL.getSequenceLength());
            assertEquals(ResetStrategy.NONE, SequenceBizType.PROJECT_INTERNAL.getResetStrategy());
        }
    }

    @Nested
    @DisplayName("枚举值测试")
    class EnumValueTest {

        @Test
        @DisplayName("枚举值数量正确")
        void enumValues_should_haveCorrectCount() {
            assertEquals(2, SequenceBizType.values().length);
        }

        @Test
        @DisplayName("valueSet不为null且不为空")
        void valueSet_should_notBeNull() {
            assertNotNull(SequenceBizType.values());
            assertTrue(SequenceBizType.values().length > 0);
        }
    }
}