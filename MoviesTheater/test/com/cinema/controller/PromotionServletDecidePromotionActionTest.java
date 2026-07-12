package com.cinema.controller;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Decision Table tests for PromotionServlet.decidePromotionAction(), extracted
 * from handleReactivate()/handleHardDelete() for testability. Covers the 6
 * rules (R1-R6) in doc/Huong_dan_Decision_Table_Testing.docx section 5.
 */
public class PromotionServletDecidePromotionActionTest {

    // TC_DT_01 / R1: Reactivate a promotion whose status is "expired" -> blocked
    @Test
    public void reactivate_expiredPromotion_isBlocked() {
        String outcome = PromotionServlet.decidePromotionAction(
                "reactivate", "expired", false, 0, null);
        assertEquals(PromotionServlet.ERROR_REACTIVATE_EXPIRED, outcome);
    }

    // TC_DT_02 / R2: Reactivate a promotion whose status is not "expired" -> succeeds
    @Test
    public void reactivate_inactivePromotion_succeeds() {
        String outcome = PromotionServlet.decidePromotionAction(
                "reactivate", "inactive", false, 0, null);
        assertEquals(PromotionServlet.SUCCESS_REACTIVATED, outcome);
    }

    // TC_DT_03 / R3: Hard-delete a promotion referenced by a paid invoice -> blocked,
    // regardless of usedCount/returnTo
    @Test
    public void hardDelete_referencedByPaidInvoice_isBlocked() {
        String outcome = PromotionServlet.decidePromotionAction(
                "hardDelete", null, true, 5, "upcoming");
        assertEquals(PromotionServlet.ERROR_DELETE_PAID_INVOICE, outcome);
    }

    // TC_DT_04 / R4: Hard-delete a used promotion from the "upcoming" tab -> blocked
    @Test
    public void hardDelete_usedFromUpcomingTab_isBlocked() {
        String outcome = PromotionServlet.decidePromotionAction(
                "hardDelete", null, false, 3, "upcoming");
        assertEquals(PromotionServlet.ERROR_DELETE_USED_UPCOMING, outcome);
    }

    // TC_DT_05 / R5: Hard-delete a used promotion from a tab other than "upcoming" -> allowed
    @Test
    public void hardDelete_usedFromOtherTab_succeeds() {
        String outcome = PromotionServlet.decidePromotionAction(
                "hardDelete", null, false, 3, "active");
        assertEquals(PromotionServlet.SUCCESS_DELETED, outcome);
    }

    // TC_DT_06 / R6: Hard-delete a never-used promotion -> allowed regardless of tab
    @Test
    public void hardDelete_neverUsed_succeeds() {
        String outcome = PromotionServlet.decidePromotionAction(
                "hardDelete", null, false, 0, "upcoming");
        assertEquals(PromotionServlet.SUCCESS_DELETED, outcome);
    }
}
