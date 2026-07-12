package com.cinema.controller;

import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for RegisterController.validateInput(fullName, email, password,
 * confirmPassword, phoneNumber). Covers Black-box (Equivalence Partitioning +
 * Boundary Value Analysis) and White-box (Statement + Decision Coverage) test
 * cases, see doc/Huong_dan_Unit_Test.docx for the design rationale.
 */
public class RegisterControllerValidateInputTest {

    private final RegisterController controller = new RegisterController();

    // TC02_01: all fields valid, with a valid phone number -> no errors
    @Test
    public void allValidWithPhone_returnsNoErrors() {
        Map<String, String> errors = controller.validateInput(
                "Nguyen Van A", "a@gmail.com", "matkhau123", "matkhau123", "0912345678");
        assertTrue(errors.isEmpty());
    }

    // TC02_02: all fields valid, phone number omitted (optional field) -> no errors
    @Test
    public void allValidWithoutPhone_returnsNoErrors() {
        Map<String, String> errors = controller.validateInput(
                "Nguyen Van A", "a@gmail.com", "matkhau123", "matkhau123", null);
        assertTrue(errors.isEmpty());
    }

    // TC02_03: fullName is null -> fullName error
    @Test
    public void nullFullName_returnsFullNameError() {
        Map<String, String> errors = controller.validateInput(
                null, "a@gmail.com", "matkhau123", "matkhau123", null);
        assertEquals("Vui lòng nhập họ tên.", errors.get("fullName"));
    }

    // TC02_04: fullName is whitespace-only (trims to empty) -> fullName error
    @Test
    public void whitespaceFullName_returnsFullNameError() {
        Map<String, String> errors = controller.validateInput(
                "   ", "a@gmail.com", "matkhau123", "matkhau123", null);
        assertEquals("Vui lòng nhập họ tên.", errors.get("fullName"));
    }

    // TC02_05: email is null -> "please enter email" error
    @Test
    public void nullEmail_returnsRequiredEmailError() {
        Map<String, String> errors = controller.validateInput(
                "Nguyen Van A", null, "matkhau123", "matkhau123", null);
        assertEquals("Vui lòng nhập email.", errors.get("email"));
    }

    // TC02_06: email has an invalid format -> "invalid email" error
    @Test
    public void malformedEmail_returnsInvalidEmailError() {
        Map<String, String> errors = controller.validateInput(
                "Nguyen Van A", "not-an-email", "matkhau123", "matkhau123", null);
        assertEquals("Email không hợp lệ.", errors.get("email"));
    }

    // TC02_07: password is null -> "please enter password" error
    @Test
    public void nullPassword_returnsRequiredPasswordError() {
        Map<String, String> errors = controller.validateInput(
                "Nguyen Van A", "a@gmail.com", null, null, null);
        assertEquals("Vui lòng nhập mật khẩu.", errors.get("password"));
    }

    // TC02_08 (boundary): password of 5 characters (below the 6-char minimum) -> length error
    @Test
    public void fiveCharPassword_returnsTooShortError() {
        Map<String, String> errors = controller.validateInput(
                "Nguyen Van A", "a@gmail.com", "abcde", "abcde", null);
        assertEquals("Mật khẩu phải có ít nhất 6 ký tự.", errors.get("password"));
    }

    // TC02_09 (boundary): password of exactly 6 characters -> no password error
    @Test
    public void sixCharPassword_returnsNoPasswordError() {
        Map<String, String> errors = controller.validateInput(
                "Nguyen Van A", "a@gmail.com", "abcdef", "abcdef", null);
        assertFalse(errors.containsKey("password"));
    }

    // TC02_10: confirmPassword does not match password -> mismatch error
    @Test
    public void mismatchedConfirmPassword_returnsMismatchError() {
        Map<String, String> errors = controller.validateInput(
                "Nguyen Van A", "a@gmail.com", "matkhau123", "other456", null);
        assertEquals("Xác nhận mật khẩu không khớp.", errors.get("confirmPassword"));
    }

    // TC02_11: phone number has an invalid format (9 digits) -> phone error
    @Test
    public void malformedPhoneNumber_returnsPhoneError() {
        Map<String, String> errors = controller.validateInput(
                "Nguyen Van A", "a@gmail.com", "matkhau123", "matkhau123", "123456789");
        assertEquals("Số điện thoại không hợp lệ (phải gồm 10 chữ số).", errors.get("phoneNumber"));
    }

    // TC02_12: phone number is an empty string (treated as not provided) -> no phone error
    @Test
    public void emptyPhoneNumber_returnsNoPhoneError() {
        Map<String, String> errors = controller.validateInput(
                "Nguyen Van A", "a@gmail.com", "matkhau123", "matkhau123", "");
        assertFalse(errors.containsKey("phoneNumber"));
    }
}
