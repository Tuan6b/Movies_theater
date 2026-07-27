function confirmDelete(scheduleId) {
    if (typeof scheduleIdsWithTickets !== 'undefined' && scheduleIdsWithTickets.has(scheduleId)) {
        const code = prompt(
            'This schedule has ticket bookings.\n' +
            'Type XOALICHCHIEUKHANCAP to confirm cancellation:'
        );
        return code === 'XOALICHCHIEUKHANCAP';
    }
    return confirm('Delete this schedule?');
}
