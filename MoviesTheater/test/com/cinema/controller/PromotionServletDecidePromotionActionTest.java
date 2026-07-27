package com.cinema.controller;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Decision Table tests for PromotionServlet.decidePromotionAction(), extracted
 * from handleReactivate()/handleHardDelete() for testability. Covers the 6
 * rules (R1-R6) in doc/Huong_dan_Decision_Table_Testing.docx section 5.
 *
 * R4/R5 now decide from the promotion's own status instead of the tab the action
 * was invoked from: the tab arrived as a hidden form field and could be edited to
 * get around the rule. The condition itself is unchanged — a used promotion that
 * has not started yet cannot be cancelled — only its source is now the database.
 */
public class PromotionServletDecidePromotionActionTest {

    // TC_DT_01 / R1: Reactivate a promotion whose status is "expired" -> blocked
    @Test
    public void reactivate_expiredPromotion_isBlocked() {
        String outcome = PromotionServlet.decidePromotionAction(
                "reactivate", "expired", false, 0);
        assertEquals(PromotionServlet.ERROR_REACTIVATE_EXPIRED, outcome);
    }

    // TC_DT_02 / R2: Reactivate a promotion whose status is not "expired" -> succeeds
    @Test
    public void reactivate_inactivePromotion_succeeds() {
        String outcome = PromotionServlet.decidePromotionAction(
                "reactivate", "inactive", false, 0);
        assertEquals(PromotionServlet.SUCCESS_REACTIVATED, outcome);
    }

    // TC_DT_03 / R3: Hard-delete a promotion referenced by a paid invoice -> blocked,
    // regardless of usedCount/status
    @Test
    public void hardDelete_referencedByPaidInvoice_isBlocked() {
        String outcome = PromotionServlet.decidePromotionAction(
                "hardDelete", "upcoming", true, 5);
        assertEquals(PromotionServlet.ERROR_DELETE_PAID_INVOICE, outcome);
    }

    // TC_DT_04 / R4: Hard-delete a used promotion that has not started yet -> blocked
    @Test
    public void hardDelete_usedUpcomingPromotion_isBlocked() {
        String outcome = PromotionServlet.decidePromotionAction(
                "hardDelete", "upcoming", false, 3);
        assertEquals(PromotionServlet.ERROR_DELETE_USED_UPCOMING, outcome);
    }

    // TC_DT_05 / R5: Hard-delete a used promotion in any other status -> allowed
    @Test
    public void hardDelete_usedActivePromotion_succeeds() {
        String outcome = PromotionServlet.decidePromotionAction(
                "hardDelete", "active", false, 3);
        assertEquals(PromotionServlet.SUCCESS_DELETED, outcome);
    }

    // TC_DT_06 / R6: Hard-delete a never-used promotion -> allowed regardless of status
    @Test
    public void hardDelete_neverUsed_succeeds() {
        String outcome = PromotionServlet.decidePromotionAction(
                "hardDelete", "upcoming", false, 0);
        assertEquals(PromotionServlet.SUCCESS_DELETED, outcome);
    }

    // Guard for the hole this rule change closed: a promotion whose status could not
    // be read (deleted concurrently) must not fall through to "allowed" for a used
    // promotion — status null is not "upcoming", so R5 applies and delete proceeds.
    // Kept explicit so the behaviour is a decision, not an accident.
    @Test
    public void hardDelete_unknownStatus_usesR5() {
        String outcome = PromotionServlet.decidePromotionAction(
                "hardDelete", null, false, 3);
        assertEquals(PromotionServlet.SUCCESS_DELETED, outcome);
    }
}
