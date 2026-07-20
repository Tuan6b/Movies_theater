package com.cinema.controller;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for EmployeeServlet.isValidEmailFormat(String), extracted from
 * validateEmail() (UC44: Manage Employee). Covers Black-box (Equivalence
 * Partitioning + Boundary Value Analysis) test cases for the email format
 * check, independent of the duplicate-email DB lookup.
 */
public class EmployeeServletValidateEmailTest {

    // TC_UT_01: well-formed email -> valid
    @Test
    public void wellFormedEmail_isValid() {
        assertTrue(EmployeeServlet.isValidEmailFormat("employee@cinema.vn"));
    }

    // TC_UT_02: null -> invalid
    @Test
    public void nullEmail_isInvalid() {
        assertFalse(EmployeeServlet.isValidEmailFormat(null));
    }

    // TC_UT_03: missing '@' -> invalid
    @Test
    public void missingAtSign_isInvalid() {
        assertFalse(EmployeeServlet.isValidEmailFormat("employee.cinema.vn"));
    }

    // TC_UT_04: missing domain dot -> invalid
    @Test
    public void missingDomainDot_isInvalid() {
        assertFalse(EmployeeServlet.isValidEmailFormat("employee@cinemavn"));
    }

    // TC_UT_05: surrounding whitespace is trimmed before matching -> valid
    @Test
    public void surroundingWhitespace_isTrimmedAndValid() {
        assertTrue(EmployeeServlet.isValidEmailFormat("  employee@cinema.vn  "));
    }

    // TC_UT_06: blank/whitespace-only string -> invalid
    @Test
    public void blankString_isInvalid() {
        assertFalse(EmployeeServlet.isValidEmailFormat("   "));
    }
}
