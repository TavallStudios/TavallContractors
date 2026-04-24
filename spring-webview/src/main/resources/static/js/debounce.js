export function debounce(callback, delayMs) {
    let timeout;
    return ((...args) => {
        if (timeout !== undefined) {
            window.clearTimeout(timeout);
        }
        timeout = window.setTimeout(() => callback(...args), delayMs);
    });
}
//# sourceMappingURL=debounce.js.map