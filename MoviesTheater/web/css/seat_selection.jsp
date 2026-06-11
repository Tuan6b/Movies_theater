<%-- 
    Document   : seat_selection
    Created on : Jun 11, 2026, 3:01:14 PM
    Author     : ADMIN
--%>

* {
    box-sizing: border-box;
}

body {
    margin: 0;
    font-family: Arial, sans-serif;
    background: #fff7f7;
    color: #261313;
}

.seat-page {
    min-height: 100vh;
}

.top-dark {
    background: #120000;
    height: 76px;
    color: white;
}

.header-inner {
    max-width: 1120px;
    height: 76px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.logo {
    color: #d00000;
    font-size: 24px;
    font-weight: 900;
    letter-spacing: 1px;
}

.nav {
    display: flex;
    gap: 26px;
}

.nav a {
    color: #dbcaca;
    text-decoration: none;
    font-size: 14px;
}

.nav a:hover {
    color: #ffffff;
}

.seat-container {
    max-width: 1120px;
    margin: 0 auto;
    padding: 38px 0 60px;
}

.booking-step {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 42px;
}

.step {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
}

.step span {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: #f3dddd;
    color: #9c7777;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 800;
}

.step p {
    margin: 0;
    color: #9c7777;
    font-size: 13px;
    font-weight: 700;
}

.step.active span {
    background: #d00000;
    color: white;
}

.step.active p {
    color: #d00000;
}

.step-line {
    width: 90px;
    height: 2px;
    background: #ead1d1;
    margin: 0 12px 26px;
}

.section-title {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 22px;
}

.title-icon {
    color: #d00000;
    font-size: 22px;
}

.section-title h1 {
    margin: 0;
    font-size: 30px;
    font-weight: 900;
}

.movie-info-card {
    background: white;
    border: 1px solid #ffc2c2;
    border-radius: 18px;
    padding: 22px 28px;
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 18px;
    margin-bottom: 28px;
}

.movie-info-card div {
    display: flex;
    flex-direction: column;
    gap: 7px;
}

.movie-info-card span {
    color: #a48b8b;
    font-size: 13px;
}

.movie-info-card strong {
    color: #2b1515;
    font-size: 15px;
}

.seat-map-card {
    background: white;
    border: 1px solid #ffc2c2;
    border-radius: 24px;
    padding: 35px 28px 30px;
}

.screen-wrapper {
    max-width: 720px;
    margin: 0 auto 45px;
    text-align: center;
}

.screen-light {
    height: 35px;
    background: radial-gradient(ellipse at center, rgba(208, 0, 0, 0.18), transparent 70%);
}

.screen {
    height: 38px;
    border-radius: 0 0 80% 80%;
    background: linear-gradient(to bottom, #4a4a4a, #111);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    letter-spacing: 6px;
    font-size: 13px;
    font-weight: 900;
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.25);
}

.seat-map {
    max-width: 860px;
    margin: 0 auto;
}

.seat-row {
    display: flex;
    align-items: center;
    margin-bottom: 13px;
}

.row-label {
    width: 34px;
    color: #d00000;
    font-weight: 900;
}

.seat-list {
    flex: 1;
    display: grid;
    grid-template-columns: repeat(10, 1fr);
    gap: 10px;
}

.seat {
    height: 38px;
    border-radius: 9px 9px 14px 14px;
    position: relative;
    cursor: pointer;
    transition: 0.18s;
    user-select: none;
}

.seat input {
    display: none;
}

.seat span {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 800;
}

.seat.normal {
    background: #fff4f4;
    border: 1px solid #ffbbbb;
    color: #d00000;
}

.seat.vip {
    background: #fff0c6;
    border: 1px solid #d6a600;
    color: #8a6400;
}

.seat.booked {
    background: #cfcfcf;
    border: 1px solid #b8b8b8;
    color: #777;
    cursor: not-allowed;
    opacity: 0.75;
}

.seat:not(.booked):hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 16px rgba(208, 0, 0, 0.14);
}

.seat input:checked + span {
    background: #d00000;
    color: white;
    border-radius: 9px 9px 14px 14px;
    box-shadow: 0 8px 16px rgba(208, 0, 0, 0.22);
}

.seat-note {
    margin-top: 32px;
    display: flex;
    justify-content: center;
    gap: 28px;
    color: #8c7777;
    font-size: 14px;
}

.seat-note div {
    display: flex;
    align-items: center;
    gap: 8px;
}

.note-box {
    width: 18px;
    height: 18px;
    border-radius: 5px;
    display: inline-block;
}

.note-box.normal {
    background: #fff4f4;
    border: 1px solid #ffbbbb;
}

.note-box.vip {
    background: #fff0c6;
    border: 1px solid #d6a600;
}

.note-box.selected {
    background: #d00000;
}

.note-box.booked {
    background: #cfcfcf;
}

.checkout-bar {
    margin-top: 24px;
    background: white;
    border: 1px solid #ffc2c2;
    border-radius: 18px;
    padding: 22px 26px;
    display: grid;
    grid-template-columns: 1.5fr 1fr auto;
    align-items: center;
    gap: 20px;
}

.selected-info p,
.price-info p {
    margin: 0 0 6px;
    color: #a48b8b;
    font-size: 13px;
}

.selected-info strong {
    color: #2b1515;
    font-size: 16px;
}

.price-info strong {
    color: #d00000;
    font-size: 24px;
}

.action-group {
    display: flex;
    gap: 12px;
}

.btn {
    min-width: 118px;
    height: 42px;
    border-radius: 999px;
    border: none;
    text-decoration: none;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-weight: 800;
    font-size: 14px;
    cursor: pointer;
}

.btn-back {
    background: #f3e8e8;
    color: #4b2828;
}

.btn-next {
    background: #d00000;
    color: white;
}

.btn-next:hover {
    background: #b40000;
}

.footer-dark {
    background: #120000;
    color: #8f7c7c;
    padding: 30px 0;
}

.footer-inner {
    max-width: 1120px;
    margin: 0 auto;
    display: flex;
    justify-content: space-between;
}

.footer-inner strong {
    color: #d00000;
}

@media (max-width: 900px) {
    .header-inner,
    .seat-container,
    .footer-inner {
        padding-left: 18px;
        padding-right: 18px;
    }

    .movie-info-card {
        grid-template-columns: repeat(2, 1fr);
    }

    .seat-list {
        grid-template-columns: repeat(5, 1fr);
    }

    .checkout-bar {
        grid-template-columns: 1fr;
    }

    .seat-note {
        flex-wrap: wrap;
    }

    .step-line {
        width: 35px;
    }
}